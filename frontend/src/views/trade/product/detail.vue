<template>
  <div class="app-container detail" v-loading="loading">
    <button type="button" class="back" @click="goBack">
      <svg viewBox="0 0 16 16" aria-hidden="true">
        <path d="M13 8H4M7.5 4.5 4 8l3.5 3.5" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" />
      </svg>
      返回
    </button>

    <div v-if="product" class="sheet">
      <!-- 左：图集 -->
      <section class="gallery">
        <div class="stage">
          <img v-if="mainImage" :src="mainImage" :alt="product.title" />
          <i v-else class="stage-letter">{{ (product.title || '·').slice(0, 1) }}</i>
        </div>
        <div v-if="product.images && product.images.length > 1" class="thumbs">
          <button
            v-for="(img, i) in product.images"
            :key="img.id ?? img.url"
            type="button"
            :class="['thumb', { on: i === activeImg }]"
            @click="activeImg = i"
          >
            <img :src="img.url" alt="" loading="lazy" />
          </button>
        </div>
      </section>

      <!-- 右：信息 -->
      <section class="info">
        <h1 class="title">{{ product.title }}</h1>

        <div class="price-row">
          <span class="price"><b>¥</b>{{ formatPrice(product.price) }}</span>
          <span v-if="product.originalPrice" class="origin">原价 ¥{{ formatPrice(product.originalPrice) }}</span>
        </div>

        <div class="chips">
          <span v-if="product.qualityGrade" class="chip chip-grade">{{ product.qualityGrade }} 级成色</span>
          <span v-if="product.conditionSelf" class="chip">{{ conditionSelfLabel(product.conditionSelf) }}</span>
          <span v-if="product.brandName" class="chip">{{ product.brandName }}</span>
        </div>

        <dl class="attrs">
          <div class="attr">
            <dt>分类</dt>
            <dd>{{ product.categoryName || '—' }}</dd>
          </div>
          <div class="attr">
            <dt>发布时间</dt>
            <dd>{{ product.createTime || '—' }}</dd>
          </div>
          <div class="attr">
            <dt>浏览</dt>
            <dd>{{ product.viewCount ?? 0 }} 次</dd>
          </div>
        </dl>

        <p class="desc">{{ product.description || '卖家没有写描述，可以先看看质检报告，或直接联系卖家问细节。' }}</p>

        <div class="actions">
          <button type="button" class="btn-primary" @click="openBuy">立即购买</button>
          <button type="button" class="btn-ghost" @click="handleFavorite">
            {{ product.favorite ? '已收藏' : '收藏' }}
          </button>
          <button type="button" class="btn-ghost" @click="goChat">联系卖家</button>
          <button type="button" class="btn-text" @click="openReport">举报</button>
        </div>
      </section>
    </div>

    <!-- 质检报告：本页的主张 -->
    <section v-if="qcReport" class="qc">
      <div class="qc-head">
        <h2 class="qc-title">智能质检报告</h2>
        <button type="button" class="qc-more" @click="goReport">
          查看完整报告
          <svg viewBox="0 0 16 16" aria-hidden="true">
            <path
              d="M3 8h9M8.5 4.5 12 8l-3.5 3.5"
              fill="none"
              stroke="currentColor"
              stroke-width="1.6"
              stroke-linecap="round"
              stroke-linejoin="round"
            />
          </svg>
        </button>
      </div>

      <div class="qc-body">
        <div class="qc-grade">
          <span class="qc-letter">{{ qcReport.report.qualityGrade || '—' }}</span>
          <span class="qc-caption">成色等级</span>
        </div>

        <div class="qc-facts">
          <div class="fact">
            <span class="k">识别机型</span>
            <span class="v">{{ qcReport.report.brandModel || '—' }}</span>
          </div>
          <div class="fact">
            <span class="k">参考区间</span>
            <span class="v"> ¥{{ formatPrice(qcReport.report.priceMin) }} – ¥{{ formatPrice(qcReport.report.priceMax) }} </span>
          </div>
          <div class="fact">
            <span class="k">缺陷摘要</span>
            <span class="v">{{ qcReport.report.defectSummary || '未记录明显缺陷' }}</span>
          </div>
        </div>
      </div>
    </section>

    <!-- 卖家 -->
    <section v-if="product" class="seller">
      <el-avatar :size="44" :src="product.sellerAvatar">{{ (product.sellerNickname || '卖').charAt(0) }}</el-avatar>
      <div class="seller-main">
        <p class="seller-name">{{ product.sellerNickname || '卖家' }}</p>
        <p class="seller-sub">综合评分 {{ product.sellerRating ?? 5.0 }}</p>
      </div>
      <button type="button" class="btn-ghost small" @click="goSeller">进入店铺</button>
    </section>

    <!-- 举报弹窗 -->
    <el-dialog v-model="reportVisible" title="举报商品" width="440px" append-to-body>
      <el-form label-width="80px">
        <el-form-item label="举报原因">
          <el-select v-model="reportReason" style="width: 100%">
            <el-option label="售假" value="售假" />
            <el-option label="虚假描述" value="虚假描述" />
            <el-option label="违禁品" value="违禁品" />
            <el-option label="价格欺诈" value="价格欺诈" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="补充说明">
          <el-input v-model="reportContent" type="textarea" :rows="3" placeholder="请描述问题(可选)" maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reportVisible = false">取消</el-button>
        <el-button type="danger" :loading="reportSubmitting" @click="submitProductReport">提交举报</el-button>
      </template>
    </el-dialog>

    <!-- 下单弹窗 -->
    <el-dialog v-model="buyVisible" title="确认订单" width="520px" append-to-body>
      <div v-if="buyProduct" class="buy-summary">
        <img v-if="buyProduct.images && buyProduct.images.length" :src="buyProduct.images[0].url" alt="" class="buy-cover" />
        <div class="buy-info">
          <p class="buy-title">{{ buyProduct.title }}</p>
          <p class="buy-price"><b>¥</b>{{ formatPrice(buyProduct.price) }}</p>
        </div>
      </div>

      <p class="buy-label">收货地址</p>
      <p v-if="addressList.length === 0" class="buy-empty">
        还没有收货地址。
        <button type="button" class="link" @click="goAddress">去添加一个</button>
      </p>
      <el-radio-group v-model="selectedAddressId" v-else class="address-group">
        <el-radio v-for="a in addressList" :key="a.id" :value="a.id" border class="address-radio">
          <div class="addr-line">{{ a.receiver }} {{ a.phone }}</div>
          <div class="addr-line sub">{{ a.province }}{{ a.city }}{{ a.district || '' }}{{ a.detail }}</div>
          <span v-if="a.isDefault === '1'" class="addr-default">默认</span>
        </el-radio>
      </el-radio-group>

      <template #footer>
        <el-button @click="buyVisible = false">取消</el-button>
        <el-button type="primary" :loading="creating" :disabled="!selectedAddressId" @click="submitOrder">
          提交订单 ¥{{ formatPrice(buyProduct?.price) }}
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="ProductDetail" lang="ts">
import { getCurrentInstance, onMounted, computed, ref, toRefs } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { getProduct, favoriteProduct, cancelFavoriteProduct } from '@/api/trade/product';
import { createOrder } from '@/api/trade/order';
import { openConversation } from '@/api/message/chat';
import { getProductReport } from '@/api/inspection';
import { submitReport } from '@/api/trade/report';
import { listAddress } from '@/api/trade/address';
import { AddressVO, ProductVO } from '@/api/trade/types';
import { ElMessage } from 'element-plus';

const { proxy } = getCurrentInstance() as any;
const { product_condition: conditionDict } = toRefs<any>(proxy?.useDict('product_condition'));

const route = useRoute();
const router = useRouter();
const loading = ref(false);
const product = ref<ProductVO>();
const qcReport = ref<any>();
const activeImg = ref(0);

const formatPrice = (v?: number) => (v == null ? '—' : Number(v).toLocaleString('zh-CN'));

const mainImage = computed(() => {
  const imgs = product.value?.images;
  if (!imgs || !imgs.length) return '';
  return imgs[Math.min(activeImg.value, imgs.length - 1)]?.url || '';
});

const load = async () => {
  loading.value = true;
  try {
    const res = await getProduct(route.query.id as string);
    product.value = res.data;
    getProductReport(route.query.id as string)
      .then((r) => {
        qcReport.value = r.data;
      })
      .catch(() => {
        qcReport.value = null;
      });
  } finally {
    loading.value = false;
  }
};

/* 举报 */
const reportVisible = ref(false);
const reportSubmitting = ref(false);
const reportReason = ref('售假');
const reportContent = ref('');

const openReport = () => {
  reportContent.value = '';
  reportVisible.value = true;
};

const submitProductReport = async () => {
  if (!product.value) return;
  reportSubmitting.value = true;
  try {
    await submitReport(product.value.id, reportReason.value, reportContent.value);
    ElMessage.success('举报已提交,管理员会尽快处理');
    reportVisible.value = false;
  } finally {
    reportSubmitting.value = false;
  }
};

const goSeller = () => {
  if (product.value?.sellerId) {
    router.push('/trade/seller?sellerId=' + product.value.sellerId);
  }
};

const goReport = () => {
  if (qcReport.value) {
    router.push('/trade/inspectionReport?id=' + qcReport.value.report.id);
  }
};

const goBack = () => {
  if (window.history.length > 1) {
    router.back();
  } else {
    router.push('/trade/market');
  }
};

const conditionSelfLabel = (val?: string) => {
  return conditionDict.value?.find((c: any) => c.value === val)?.label || val || '-';
};

const handleFavorite = async () => {
  if (!product.value) return;
  try {
    if (product.value.favorite) {
      await cancelFavoriteProduct(product.value.id);
      ElMessage.success('已取消收藏');
    } else {
      await favoriteProduct(product.value.id);
      ElMessage.success('收藏成功');
    }
    product.value.favorite = !product.value.favorite;
  } catch {
    // 未登录等异常已由拦截器提示
  }
};

const goChat = async () => {
  if (!product.value) return;
  const res = await openConversation(product.value.id);
  // 消息菜单挂在 /trade 下，路由是 /trade/chat
  router.push('/trade/chat?conversationId=' + res.data);
};

/* 立即购买 */
const buyVisible = ref(false);
const creating = ref(false);
const addressList = ref<AddressVO[]>([]);
const selectedAddressId = ref<number>();
const buyProduct = ref<ProductVO>();

const openBuy = async () => {
  if (!product.value) return;
  buyProduct.value = product.value;
  buyVisible.value = true;
  const res = await listAddress();
  addressList.value = res.data;
  const def = addressList.value.find((a) => a.isDefault === '1');
  selectedAddressId.value = def ? def.id : addressList.value.length ? addressList.value[0].id : undefined;
};

const goAddress = () => {
  buyVisible.value = false;
  router.push('/trade/address');
};

const submitOrder = async () => {
  if (!buyProduct.value || !selectedAddressId.value) return;
  creating.value = true;
  try {
    const res = await createOrder({ productId: buyProduct.value.id, addressId: selectedAddressId.value });
    ElMessage.success('下单成功,请支付');
    buyVisible.value = false;
    router.push('/trade/pay?id=' + res.data);
  } finally {
    creating.value = false;
  }
};

onMounted(() => {
  load();
});
</script>

<style lang="scss" scoped>
.detail {
  max-width: 1180px;
}

/* ---------- 返回 ---------- */
.back {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  margin-bottom: 16px;
  padding: 7px 13px 7px 9px;
  border: 1px solid var(--sm-card-border);
  border-radius: var(--sm-r-pill);
  background: var(--sm-paper);
  font-family: inherit;
  font-size: 12.5px;
  font-weight: 700;
  color: var(--sm-ink-2);
  cursor: pointer;
  transition:
    border-color 0.16s ease,
    color 0.16s ease;

  svg {
    width: 14px;
    height: 14px;
  }

  &:hover {
    border-color: var(--sm-line);
    color: var(--sm-ink);
  }
}

/* ---------- 主区：图 + 信息 ---------- */
.sheet {
  display: grid;
  grid-template-columns: minmax(0, 1.05fr) minmax(0, 1fr);
  gap: 36px;
  align-items: start;
  padding: 24px;
  border: 1px solid var(--sm-card-border);
  border-radius: 22px;
  background: var(--sm-paper);
}

.stage {
  aspect-ratio: 4 / 3;
  border-radius: 16px;
  overflow: hidden;
  background: var(--sm-brand-tint);
  display: flex;
  align-items: center;
  justify-content: center;

  img {
    width: 100%;
    height: 100%;
    object-fit: contain;
    display: block;
  }
}

.stage-letter {
  font-style: normal;
  font-size: 46px;
  font-weight: 800;
  color: var(--sm-brand-deep);
  opacity: 0.4;
}

.thumbs {
  display: flex;
  gap: 8px;
  margin-top: 10px;
  overflow-x: auto;
  padding-bottom: 2px;
}

.thumb {
  flex: 0 0 62px;
  width: 62px;
  height: 62px;
  padding: 0;
  border: 2px solid transparent;
  border-radius: 10px;
  overflow: hidden;
  background: var(--sm-brand-tint);
  cursor: pointer;
  transition: border-color 0.16s ease;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
  }

  &.on {
    border-color: var(--sm-brand);
  }
}

.info {
  min-width: 0;
  padding-top: 6px;
}

.title {
  margin: 0;
  font-size: 24px;
  font-weight: 800;
  line-height: 1.35;
  letter-spacing: -0.02em;
  color: var(--sm-ink);
}

.price-row {
  display: flex;
  align-items: baseline;
  gap: 10px;
  margin-top: 14px;
}

.price {
  font-family: var(--sm-font-num);
  font-size: 34px;
  font-weight: 800;
  letter-spacing: -0.03em;
  color: var(--sm-ink);
  font-variant-numeric: tabular-nums;

  b {
    margin-right: 2px;
    font-size: 18px;
    font-weight: 700;
    color: var(--sm-stone);
  }
}

.origin {
  font-size: 13px;
  font-weight: 500;
  color: var(--sm-stone);
  text-decoration: line-through;
}

.chips {
  display: flex;
  flex-wrap: wrap;
  gap: 7px;
  margin-top: 14px;
}

.chip {
  padding: 4px 11px;
  border-radius: var(--sm-r-pill);
  background: var(--sm-bg);
  border: 1px solid var(--sm-line-soft);
  font-size: 12px;
  font-weight: 700;
  color: var(--sm-ink-3);
}

.chip-grade {
  background: var(--sm-brand-soft);
  border-color: transparent;
  color: var(--sm-brand-deep);
}

/* 属性：三行键值，不用带边框的表格 */
.attrs {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin: 22px 0 0;
  padding: 16px 0;
  border-top: 1px solid var(--sm-line-soft);
  border-bottom: 1px solid var(--sm-line-soft);

  .attr {
    min-width: 0;
  }

  dt {
    font-size: 11.5px;
    font-weight: 600;
    color: var(--sm-stone);
  }

  dd {
    margin: 4px 0 0;
    font-size: 13px;
    font-weight: 700;
    color: var(--sm-ink);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.desc {
  margin: 18px 0 0;
  font-size: 13.5px;
  font-weight: 500;
  line-height: 1.85;
  color: var(--sm-ink-2);
  white-space: pre-wrap;
}

/* ---------- 行动按钮 ---------- */
.actions {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 10px;
  margin-top: 26px;
}

.btn-primary,
.btn-ghost,
.btn-text {
  font-family: inherit;
  cursor: pointer;
  transition:
    transform 0.16s var(--sm-ease),
    box-shadow 0.16s var(--sm-ease),
    background-color 0.16s ease,
    border-color 0.16s ease,
    color 0.16s ease;
}

.btn-primary {
  height: 44px;
  padding: 0 30px;
  border: none;
  border-radius: var(--sm-r-pill);
  background: var(--sm-brand);
  color: #fff;
  font-size: 14.5px;
  font-weight: 700;
  letter-spacing: 0.01em;
  box-shadow: 0 3px 0 var(--sm-brand-deep);

  &:hover {
    background: #3aa6f2;
  }

  &:active {
    transform: translateY(3px);
    box-shadow: 0 0 0 var(--sm-brand-deep);
  }
}

.btn-ghost {
  height: 44px;
  padding: 0 22px;
  border: 1px solid var(--sm-line);
  border-radius: var(--sm-r-pill);
  background: var(--sm-paper);
  font-size: 13.5px;
  font-weight: 700;
  color: var(--sm-ink-2);

  &:hover {
    border-color: var(--sm-brand);
    color: var(--sm-brand-deep);
  }

  &.small {
    height: 34px;
    padding: 0 16px;
    font-size: 12.5px;
  }
}

.btn-text {
  height: 44px;
  padding: 0 8px;
  border: none;
  background: none;
  font-size: 13px;
  font-weight: 600;
  color: var(--sm-stone);

  &:hover {
    color: var(--sm-danger);
  }
}

/* ---------- 质检报告 ---------- */
.qc {
  margin-top: 18px;
  padding: 22px 24px 24px;
  border: 1px solid var(--sm-line);
  border-radius: 22px;
  background: linear-gradient(120deg, #eef7ff 0%, #f7fbff 58%, #ffffff 100%);
}

.qc-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 16px;
}

.qc-title {
  margin: 0;
  font-size: 16px;
  font-weight: 800;
  letter-spacing: -0.01em;
  color: var(--sm-ink);
}

.qc-more {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 0;
  border: none;
  background: none;
  font-family: inherit;
  font-size: 13px;
  font-weight: 700;
  color: var(--sm-brand-deep);
  cursor: pointer;

  svg {
    width: 14px;
    height: 14px;
    transition: transform 0.2s var(--sm-ease);
  }

  &:hover svg {
    transform: translateX(3px);
  }
}

.qc-body {
  display: flex;
  align-items: center;
  gap: 26px;
  margin-top: 18px;
}

.qc-grade {
  flex: 0 0 auto;
  width: 92px;
  height: 92px;
  border-radius: 20px;
  background: var(--sm-paper);
  border: 1px solid var(--sm-line);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 2px;
}

.qc-letter {
  font-family: var(--sm-font-num);
  font-size: 34px;
  font-weight: 800;
  line-height: 1;
  color: var(--sm-brand-deep);
}

.qc-caption {
  font-size: 11px;
  font-weight: 600;
  color: var(--sm-stone);
}

.qc-facts {
  display: grid;
  grid-template-columns: minmax(0, 0.9fr) minmax(0, 1fr) minmax(0, 1.3fr);
  gap: 16px 26px;
  min-width: 0;
  flex: 1;
}

.fact {
  min-width: 0;
}

.fact .k {
  display: block;
  font-size: 11.5px;
  font-weight: 600;
  color: var(--sm-stone);
}

.fact .v {
  display: block;
  margin-top: 3px;
  font-size: 14px;
  font-weight: 700;
  line-height: 1.6;
  color: var(--sm-ink);
}

/* ---------- 卖家 ---------- */
.seller {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 14px;
  padding: 14px 20px;
  border: 1px solid var(--sm-card-border);
  border-radius: 18px;
  background: var(--sm-paper);
}

.seller-main {
  flex: 1;
  min-width: 0;
}

.seller-name {
  margin: 0;
  font-size: 14px;
  font-weight: 800;
  color: var(--sm-ink);
}

.seller-sub {
  margin: 2px 0 0;
  font-size: 12px;
  font-weight: 600;
  color: var(--sm-stone);
}

/* ---------- 下单弹窗 ---------- */
.buy-summary {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  border-radius: 14px;
  background: var(--sm-bg);
}

.buy-cover {
  width: 64px;
  height: 64px;
  border-radius: 10px;
  object-fit: cover;
  flex-shrink: 0;
}

.buy-info {
  min-width: 0;
}

.buy-title {
  margin: 0;
  font-size: 13.5px;
  font-weight: 700;
  color: var(--sm-ink);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.buy-price {
  margin: 4px 0 0;
  font-family: var(--sm-font-num);
  font-size: 18px;
  font-weight: 800;
  color: var(--sm-ink);

  b {
    margin-right: 1px;
    font-size: 12px;
    color: var(--sm-stone);
  }
}

.buy-label {
  margin: 18px 0 10px;
  font-size: 13px;
  font-weight: 800;
  color: var(--sm-ink);
}

.buy-empty {
  margin: 0;
  font-size: 13px;
  color: var(--sm-stone);
}

.link {
  padding: 0;
  border: none;
  background: none;
  font-family: inherit;
  font-size: 13px;
  font-weight: 700;
  color: var(--sm-brand-deep);
  cursor: pointer;
}

.address-group {
  display: flex;
  flex-direction: column;
  gap: 10px;
  width: 100%;
}

.address-radio {
  width: 100%;
  margin-right: 0 !important;
  height: auto;
  padding: 11px 14px;
  border-radius: 12px;

  :deep(.el-radio__label) {
    width: 100%;
  }

  .addr-line {
    line-height: 1.6;
    font-weight: 600;
    color: var(--sm-ink);

    &.sub {
      color: var(--sm-stone);
      font-size: 12px;
      font-weight: 500;
    }
  }

  .addr-default {
    display: inline-block;
    margin-top: 3px;
    padding: 1px 7px;
    border-radius: 5px;
    background: var(--sm-accent-soft);
    color: #1c7a52;
    font-size: 10.5px;
    font-weight: 700;
  }
}

/* ---------- 响应式 ---------- */
@media (max-width: 900px) {
  .sheet {
    grid-template-columns: minmax(0, 1fr);
    gap: 20px;
    padding: 16px;
  }

  .title {
    font-size: 19px;
  }

  .price {
    font-size: 28px;
  }

  .attrs {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .qc-body {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }

  .qc-facts {
    grid-template-columns: minmax(0, 1fr);
    width: 100%;
  }
}
</style>
