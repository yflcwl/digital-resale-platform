<template>
  <div class="app-container" v-loading="loading">
    <el-button icon="ArrowLeft" @click="goBack" style="margin-bottom: 12px">返回</el-button>
    <el-row :gutter="20" v-if="product">
      <!-- 左侧轮播 -->
      <el-col :span="10">
        <el-card shadow="never">
          <el-carousel v-if="product.images && product.images.length > 0" height="380px" trigger="click">
            <el-carousel-item v-for="img in product.images" :key="img.id ?? img.url">
              <el-image :src="img.url" fit="contain" style="width: 100%; height: 100%" />
            </el-carousel-item>
          </el-carousel>
          <el-empty v-else description="暂无图片" :image-size="80" />
        </el-card>
      </el-col>

      <!-- 右侧信息 -->
      <el-col :span="14">
        <el-card shadow="never">
          <h2 class="detail-title">{{ product.title }}</h2>
          <div class="price-box">
            <span class="price">¥{{ product.price }}</span>
            <span v-if="product.originalPrice" class="original-price">原价 ¥{{ product.originalPrice }}</span>
            <el-tag v-if="product.qualityGrade" type="warning" size="large" style="margin-left: 12px">{{ product.qualityGrade }}级成色</el-tag>
            <el-tag v-if="product.conditionSelf" type="info" size="large" style="margin-left: 8px">{{ conditionSelfLabel(product.conditionSelf) }}</el-tag>
          </div>

          <el-descriptions :column="2" border class="detail-desc">
            <el-descriptions-item label="分类">{{ product.categoryName }}</el-descriptions-item>
            <el-descriptions-item label="品牌">{{ product.brandName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="发布时间">{{ product.createTime }}</el-descriptions-item>
            <el-descriptions-item label="浏览">{{ product.viewCount }}</el-descriptions-item>
          </el-descriptions>

          <div class="description-block">
            <h4>商品描述</h4>
            <p>{{ product.description || '卖家很懒,没有写描述' }}</p>
          </div>

          <div class="action-bar">
            <el-button type="danger" size="large" @click="openBuy">立即购买</el-button>
            <el-button type="primary" size="large" @click="handleFavorite">
              {{ product.favorite ? '已收藏' : '收藏' }}
            </el-button>
            <el-button size="large" @click="goChat">联系卖家</el-button>
          </div>
        </el-card>

        <!-- 质检报告 -->
        <el-card shadow="never" class="seller-card" v-if="qcReport">
          <template #header>
            <div class="qc-header">
              <span>智能质检报告</span>
              <el-tag type="warning" size="small">{{ qcReport.report.qualityGrade }} 级</el-tag>
            </div>
          </template>
          <div class="qc-body">
            <div class="qc-meta">
              <span>识别机型:{{ qcReport.report.brandModel }}</span>
              <span>参考价:¥{{ qcReport.report.priceMin }} ~ ¥{{ qcReport.report.priceMax }}</span>
            </div>
            <div class="qc-defects">{{ qcReport.report.defectSummary }}</div>
            <el-button link type="primary" @click="goReport">查看完整报告</el-button>
          </div>
        </el-card>
        <!-- 卖家信息 -->
        <el-card shadow="never" class="seller-card">
          <template #header><span>卖家信息</span></template>
          <div class="seller-info">
            <el-avatar :size="48" :src="product.sellerAvatar">{{ (product.sellerNickname || '卖').charAt(0) }}</el-avatar>
            <div class="seller-meta">
              <div class="seller-name">{{ product.sellerNickname || '卖家' }}</div>
              <div class="seller-rating">综合评分:{{ product.sellerRating ?? 5.0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 下单弹窗 -->
    <el-dialog v-model="buyVisible" title="确认订单" width="520px" append-to-body>
      <div v-if="buyProduct" class="buy-summary">
        <el-image :src="buyProduct.images && buyProduct.images.length ? buyProduct.images[0].url : ''" fit="cover" class="buy-cover" />
        <div>
          <div class="buy-title">{{ buyProduct.title }}</div>
          <div class="buy-price">¥{{ buyProduct.price }}</div>
        </div>
      </div>
      <el-divider />
      <h4>选择收货地址</h4>
      <el-empty v-if="addressList.length === 0" description="还没有收货地址" :image-size="60">
        <el-button type="primary" size="small" @click="goAddress">去添加地址</el-button>
      </el-empty>
      <el-radio-group v-model="selectedAddressId" v-else class="address-group">
        <el-radio v-for="a in addressList" :key="a.id" :value="a.id" border class="address-radio">
          <div class="addr-line">{{ a.receiver }} {{ a.phone }}</div>
          <div class="addr-line sub">{{ a.province }}{{ a.city }}{{ a.district || '' }}{{ a.detail }}</div>
          <el-tag v-if="a.isDefault === '1'" type="success" size="small">默认</el-tag>
        </el-radio>
      </el-radio-group>
      <template #footer>
        <el-button @click="buyVisible = false">取消</el-button>
        <el-button type="danger" :loading="creating" :disabled="!selectedAddressId" @click="submitOrder">
          提交订单(¥{{ buyProduct?.price }})
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="ProductDetail" lang="ts">
import { getCurrentInstance, onMounted, ref, toRefs } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { getProduct, favoriteProduct, cancelFavoriteProduct } from '@/api/trade/product';
import { createOrder } from '@/api/trade/order';
import { openConversation } from '@/api/message/chat';
import { getProductReport } from '@/api/inspection';
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

const goReport = () => {
  if (qcReport.value) {
    router.push('/inspection/report?id=' + qcReport.value.report.id);
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
  router.push('/message/chat?conversationId=' + res.data);
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
.detail-title {
  margin: 0 0 12px;
  font-size: 22px;
}
.price-box {
  background: #fdf3f3;
  padding: 14px;
  border-radius: 4px;
  margin-bottom: 16px;
  .price {
    color: var(--sm-danger);
    font-size: 28px;
    font-weight: 700;
  }
  .original-price {
    margin-left: 10px;
    color: var(--sm-ink-3);
    text-decoration: line-through;
    font-size: 14px;
  }
}
.detail-desc {
  margin-bottom: 16px;
}
.description-block {
  margin-bottom: 20px;
  h4 {
    margin: 0 0 8px;
  }
  p {
    color: var(--sm-ink-2);
    line-height: 1.8;
  }
}
.action-bar {
  margin-bottom: 16px;
}
.seller-card {
  .seller-info {
    display: flex;
    align-items: center;
    .seller-meta {
      margin-left: 14px;
      .seller-name {
        font-size: 16px;
        font-weight: 600;
      }
      .seller-rating {
        margin-top: 4px;
        color: var(--sm-warning);
        font-size: 13px;
      }
    }
  }
}
</style>

<style lang="scss" scoped>
.buy-summary {
  display: flex;
  gap: 12px;
  .buy-cover {
    width: 72px;
    height: 72px;
    border-radius: 6px;
  }
  .buy-title {
    font-size: 14px;
    font-weight: 500;
  }
  .buy-price {
    margin-top: 8px;
    color: var(--sm-danger);
    font-size: 20px;
    font-weight: 700;
  }
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
  padding: 10px 14px;
  :deep(.el-radio__label) {
    width: 100%;
  }
  .addr-line {
    line-height: 1.6;
    &.sub {
      color: var(--sm-ink-3);
      font-size: 12px;
    }
  }
}
</style>
