<template>
  <div class="app-container od" v-loading="loading">
    <button type="button" class="back" @click="goBack">
      <svg viewBox="0 0 16 16" aria-hidden="true">
        <path d="M13 8H4M7.5 4.5 4 8l3.5 3.5" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" />
      </svg>
      返回
    </button>

    <template v-if="order">
      <!-- 状态横幅：随状态变色，操作按钮就地给出 -->
      <section :class="['banner', bannerClass]">
        <div class="b-main">
          <p class="b-status">{{ statusLabel(order.status) }}</p>
          <p class="b-sub">
            <span class="b-no">订单号 {{ order.orderNo }}</span>
            <i></i>
            <span>下单于 {{ order.createTime }}</span>
          </p>
        </div>
        <div class="b-actions">
          <template v-if="canPay">
            <button type="button" class="btn-ghost light" @click="handleCancel">取消订单</button>
            <button type="button" class="btn-primary" @click="goPay">去支付</button>
          </template>
          <button v-if="canShip" type="button" class="btn-primary" @click="openShip">去发货</button>
          <button v-if="canReceive" type="button" class="btn-primary" @click="handleReceive">确认收货</button>
          <button v-if="canReview" type="button" class="btn-primary" @click="openReview">评价</button>
        </div>
      </section>

      <!-- 主体：商品/地址 + 交易信息 -->
      <div class="sheet">
        <section class="col">
          <h2 class="sec-title">商品</h2>
          <div class="goods" role="link" tabindex="0" @click="goProduct" @keydown.enter="goProduct">
            <div class="g-cover">
              <img v-if="order.productImage" :src="order.productImage" alt="" />
              <i v-else class="g-letter">{{ (order.productTitle || '·').slice(0, 1) }}</i>
            </div>
            <div class="g-main">
              <p class="g-title">{{ order.productTitle }}</p>
              <p class="g-hint">点击查看商品与质检报告</p>
            </div>
            <p class="g-price"><b>¥</b>{{ formatPrice(order.price) }}</p>
          </div>

          <h2 class="sec-title mt">收货地址</h2>
          <p class="addr">
            <span class="a-receiver">{{ address?.receiver }} {{ address?.phone }}</span>
            <span class="a-detail">{{ address?.fullAddress }}</span>
          </p>
          <p v-if="order.expressCompany" class="express">{{ order.expressCompany }} {{ order.expressNo }}</p>
        </section>

        <section class="col side">
          <h2 class="sec-title">交易信息</h2>
          <dl class="kv">
            <div class="row">
              <dt>买家</dt>
              <dd>{{ order.buyerNickname || '—' }}</dd>
            </div>
            <div class="row">
              <dt>卖家</dt>
              <dd>{{ order.sellerNickname || '—' }}</dd>
            </div>
            <div class="row">
              <dt>支付时间</dt>
              <dd>{{ order.payTime || '—' }}</dd>
            </div>
            <div class="row">
              <dt>发货时间</dt>
              <dd>{{ order.shipTime || '—' }}</dd>
            </div>
            <div class="row">
              <dt>收货时间</dt>
              <dd>{{ order.receiveTime || '—' }}</dd>
            </div>
          </dl>
        </section>
      </div>

      <!-- 流转：主流程步进 + 明细时间线 -->
      <section v-if="!isCanceled" class="flow panel">
        <h2 class="sec-title">订单流转</h2>
        <div class="steps">
          <template v-for="(s, i) in FLOW" :key="s.status">
            <div :class="['step', { done: i <= stepIndex, now: i === stepIndex }]">
              <span class="s-dot"></span>
              <span class="s-label">{{ s.label }}</span>
            </div>
            <div v-if="i < FLOW.length - 1" :class="['s-line', { done: i < stepIndex }]"></div>
          </template>
        </div>

        <ul v-if="order.statusLogs && order.statusLogs.length" class="logs">
          <li v-for="log in order.statusLogs" :key="log.id" class="log">
            <span class="l-time">{{ log.createTime }}</span>
            <span class="l-status">{{ statusLabel(log.toStatus) }}</span>
            <span v-if="log.remark" class="l-remark">{{ log.remark }}</span>
          </li>
        </ul>
      </section>
    </template>

    <p v-else-if="!loading" class="empty">订单不存在或已被删除。</p>

    <!-- 发货弹窗 -->
    <el-dialog v-model="shipVisible" title="填写发货信息" width="440px" append-to-body>
      <el-form label-width="90px">
        <el-form-item label="快递公司">
          <el-select v-model="shipForm.expressCompany" style="width: 100%">
            <el-option label="顺丰速运" value="顺丰速运" />
            <el-option label="中通快递" value="中通快递" />
            <el-option label="圆通速递" value="圆通速递" />
            <el-option label="韵达快递" value="韵达快递" />
            <el-option label="京东物流" value="京东物流" />
          </el-select>
        </el-form-item>
        <el-form-item label="快递单号">
          <el-input v-model="shipForm.expressNo" placeholder="请输入快递单号" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="shipVisible = false">取消</el-button>
        <el-button type="primary" :loading="shipping" @click="submitShip">确认发货</el-button>
      </template>
    </el-dialog>

    <!-- 评价弹窗 -->
    <el-dialog v-model="reviewVisible" title="评价订单" width="440px" append-to-body>
      <el-form label-width="70px">
        <el-form-item label="评分">
          <el-rate v-model="reviewForm.rating" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="reviewForm.content" type="textarea" :rows="3" maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewVisible = false">取消</el-button>
        <el-button type="primary" :loading="reviewing" @click="submitReview">提交评价</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="OrderDetail" lang="ts">
import { computed, getCurrentInstance, onMounted, reactive, ref, toRefs } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { getOrder, cancelOrder, receiveOrder, shipOrder, reviewOrder } from '@/api/trade/order';
import { OrderVO } from '@/api/trade/types';
import { useUserStore } from '@/store/modules/user';
import { ElMessage, ElMessageBox } from 'element-plus';

const { proxy } = getCurrentInstance() as any;
const { order_status: orderStatusDict } = toRefs<any>(proxy?.useDict('order_status'));

const statusLabel = (v: string) => orderStatusDict.value?.find((d: any) => d.value === v)?.label || v;

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();
const loading = ref(false);
const order = ref<OrderVO>();

const formatPrice = (v?: number) => (v == null ? '—' : Number(v).toLocaleString('zh-CN'));

const myId = computed(() => userStore.userId);
const isBuyer = computed(() => order.value && Number(order.value.buyerId) === Number(myId.value));
const isSeller = computed(() => order.value && Number(order.value.sellerId) === Number(myId.value));

const canPay = computed(() => isBuyer.value && order.value?.status === 'PENDING_PAY');
const canCancel = computed(() => isBuyer.value && order.value?.status === 'PENDING_PAY');
const canReceive = computed(() => isBuyer.value && order.value?.status === 'SHIPPED');
const canReview = computed(() => isBuyer.value && order.value?.status === 'RECEIVED');
const canShip = computed(() => isSeller.value && order.value?.status === 'PAID');

/** 主流程步进：取消态不展示 */
const FLOW = [
  { status: 'PENDING_PAY', label: '下单' },
  { status: 'PAID', label: '支付' },
  { status: 'SHIPPED', label: '发货' },
  { status: 'RECEIVED', label: '收货' },
  { status: 'REVIEWED', label: '评价' }
] as const;

const isCanceled = computed(() => order.value?.status === 'CANCELED');

const stepIndex = computed(() => {
  const st = order.value?.status;
  const idx = FLOW.findIndex((s) => s.status === st);
  if (idx >= 0) return idx;
  if (st === 'FINISHED') return FLOW.length - 1;
  return 0;
});

const address = computed<{ receiver?: string; phone?: string; fullAddress?: string } | null>(() => {
  if (!order.value?.addressSnapshot) return null;
  try {
    return JSON.parse(order.value.addressSnapshot);
  } catch {
    return null;
  }
});

/** 状态横幅配色 */
const bannerClass = computed(() => {
  const st = order.value?.status;
  if (st === 'PENDING_PAY') return 'is-wait';
  if (st === 'PAID' || st === 'SHIPPED') return 'is-going';
  if (st === 'RECEIVED' || st === 'REVIEWED' || st === 'FINISHED') return 'is-done';
  return 'is-idle';
});

const load = async () => {
  loading.value = true;
  try {
    const res = await getOrder(route.query.id as string);
    order.value = res.data;
  } finally {
    loading.value = false;
  }
};

const goBack = () => {
  if (window.history.length > 1) {
    router.back();
  } else {
    router.push('/trade/order');
  }
};

const goPay = () => {
  router.push('/trade/pay?id=' + order.value?.id);
};

const goProduct = () => {
  if (order.value) {
    router.push('/trade/detail?id=' + order.value.productId);
  }
};

const handleCancel = async () => {
  await ElMessageBox.confirm('确认取消该订单?取消后商品将重新上架。', '提示', { type: 'warning' });
  await cancelOrder(order.value!.id);
  ElMessage.success('订单已取消');
  load();
};

const handleReceive = async () => {
  await ElMessageBox.confirm('确认已收到商品?', '提示', { type: 'warning' });
  await receiveOrder(order.value!.id);
  ElMessage.success('确认收货成功');
  load();
};

/* 发货 */
const shipVisible = ref(false);
const shipping = ref(false);
const shipForm = reactive({ expressCompany: '顺丰速运', expressNo: '' });

const openShip = () => {
  shipVisible.value = true;
};

const submitShip = async () => {
  if (!shipForm.expressNo) {
    ElMessage.warning('请输入快递单号');
    return;
  }
  shipping.value = true;
  try {
    await shipOrder(order.value!.id, shipForm);
    ElMessage.success('发货成功');
    shipVisible.value = false;
    load();
  } finally {
    shipping.value = false;
  }
};

/* 评价 */
const reviewVisible = ref(false);
const reviewing = ref(false);
const reviewForm = reactive({ rating: 5, content: '' });

const openReview = () => {
  reviewVisible.value = true;
};

const submitReview = async () => {
  reviewing.value = true;
  try {
    await reviewOrder(order.value!.id, reviewForm);
    ElMessage.success('评价成功');
    reviewVisible.value = false;
    load();
  } finally {
    reviewing.value = false;
  }
};

onMounted(() => {
  load();
});
</script>

<style lang="scss" scoped>
.od {
  max-width: 1020px;
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

/* ---------- 状态横幅 ---------- */
.banner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 14px;
  padding: 20px 26px;
  border-radius: 20px;
  color: #fff;
  overflow: hidden;

  .b-status {
    margin: 0;
    font-size: 22px;
    font-weight: 800;
    letter-spacing: -0.01em;
  }

  .b-sub {
    display: flex;
    align-items: center;
    gap: 10px;
    margin: 7px 0 0;
    font-size: 12px;
    font-weight: 500;
    opacity: 0.85;

    i {
      width: 3px;
      height: 3px;
      border-radius: 50%;
      background: currentColor;
    }

    .b-no {
      font-family: var(--sm-font-num);
      font-variant-numeric: tabular-nums;
    }
  }

  .b-actions {
    display: flex;
    align-items: center;
    flex-wrap: wrap;
    gap: 10px;
  }

  /* 语义色：待办琥珀 / 进行中蓝 / 完成绿 / 其他灰蓝 */
  &.is-wait {
    background: linear-gradient(120deg, #b97b0a, #d99a1f);
    box-shadow: 0 14px 30px -16px rgba(185, 123, 10, 0.6);
  }

  &.is-going {
    background: linear-gradient(120deg, #0e6fb8, #2b9cf0);
    box-shadow: 0 14px 30px -16px rgba(14, 111, 184, 0.6);
  }

  &.is-done {
    background: linear-gradient(120deg, #2f9e6e, #4fc98a);
    box-shadow: 0 14px 30px -16px rgba(47, 158, 110, 0.6);
  }

  &.is-idle {
    background: linear-gradient(120deg, #51617a, #8095ab);
    box-shadow: 0 14px 30px -16px rgba(81, 97, 122, 0.6);
  }
}

.btn-primary,
.btn-ghost {
  font-family: inherit;
  cursor: pointer;
  border-radius: var(--sm-r-pill);
  transition:
    transform 0.16s var(--sm-ease),
    box-shadow 0.16s var(--sm-ease),
    background-color 0.16s ease,
    border-color 0.16s ease;
}

.btn-primary {
  height: 40px;
  padding: 0 24px;
  border: none;
  background: #fff;
  color: var(--sm-ink);
  font-size: 13.5px;
  font-weight: 800;

  &:hover {
    transform: translateY(-1px);
  }

  &:active {
    transform: translateY(1px);
  }
}

.btn-ghost {
  height: 40px;
  padding: 0 20px;
  background: transparent;
  border: 1px solid rgba(255, 255, 255, 0.55);
  color: #fff;
  font-size: 13px;
  font-weight: 700;

  &:hover {
    background: rgba(255, 255, 255, 0.14);
  }
}

/* ---------- 主体 ---------- */
.sheet {
  display: grid;
  grid-template-columns: minmax(0, 1.35fr) minmax(0, 1fr);
  gap: 0 36px;
  margin-top: 16px;
  padding: 22px 26px;
  border: 1px solid var(--sm-card-border);
  border-radius: 20px;
  background: var(--sm-paper);
}

.col {
  min-width: 0;

  &.side {
    padding-left: 36px;
    border-left: 1px solid var(--sm-line-soft);
  }
}

.sec-title {
  margin: 0 0 14px;
  font-size: 14px;
  font-weight: 800;
  letter-spacing: -0.01em;
  color: var(--sm-ink);

  &.mt {
    margin-top: 22px;
  }
}

/* 商品行 */
.goods {
  display: flex;
  align-items: center;
  gap: 13px;
  padding: 4px 0;
  cursor: pointer;

  &:hover .g-title {
    color: var(--sm-brand-deep);
  }

  &:focus-visible {
    outline: 2px solid var(--sm-brand);
    outline-offset: 3px;
    border-radius: 4px;
  }
}

.g-cover {
  flex: 0 0 64px;
  width: 64px;
  height: 64px;
  border-radius: 12px;
  overflow: hidden;
  background: var(--sm-brand-tint);
  display: flex;
  align-items: center;
  justify-content: center;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
  }
}

.g-letter {
  font-style: normal;
  font-size: 20px;
  font-weight: 800;
  color: var(--sm-brand-deep);
  opacity: 0.4;
}

.g-main {
  flex: 1;
  min-width: 0;
}

.g-title {
  margin: 0;
  font-size: 14px;
  font-weight: 700;
  line-height: 1.5;
  color: var(--sm-ink);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  transition: color 0.18s ease;
}

.g-hint {
  margin: 4px 0 0;
  font-size: 11.5px;
  font-weight: 500;
  color: var(--sm-stone);
}

.g-price {
  flex-shrink: 0;
  margin: 0;
  padding-left: 8px;
  font-family: var(--sm-font-num);
  font-size: 19px;
  font-weight: 800;
  letter-spacing: -0.02em;
  color: var(--sm-ink);
  font-variant-numeric: tabular-nums;

  b {
    margin-right: 1px;
    font-size: 12px;
    font-weight: 700;
    color: var(--sm-stone);
  }
}

/* 地址 */
.addr {
  margin: 0;
  line-height: 1.7;
}

.a-receiver {
  display: block;
  font-size: 13.5px;
  font-weight: 700;
  color: var(--sm-ink);
}

.a-detail {
  display: block;
  margin-top: 2px;
  font-size: 12.5px;
  font-weight: 500;
  color: var(--sm-ink-2);
}

.express {
  display: inline-block;
  margin: 12px 0 0;
  padding: 6px 12px;
  border-radius: 10px;
  background: var(--sm-bg);
  font-family: var(--sm-font-num);
  font-size: 12px;
  font-weight: 700;
  color: var(--sm-ink-2);
}

/* 交易信息键值 */
.kv {
  margin: 0;
}

.kv .row {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  padding: 9px 0;

  & + .row {
    border-top: 1px solid var(--sm-line-soft);
  }

  dt {
    flex-shrink: 0;
    font-size: 12px;
    font-weight: 600;
    color: var(--sm-stone);
  }

  dd {
    margin: 0;
    font-family: var(--sm-font-num);
    font-size: 12.5px;
    font-weight: 600;
    color: var(--sm-ink);
    text-align: right;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

/* ---------- 流转 ---------- */
.flow {
  margin-top: 16px;
  padding: 20px 26px 22px;
  border: 1px solid var(--sm-card-border);
  border-radius: 20px;
  background: var(--sm-paper);
}

.steps {
  display: flex;
  align-items: center;
  margin-top: 6px;
}

.step {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.s-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: var(--sm-line);
  border: 3px solid var(--sm-bg-2);
  box-sizing: content-box;
}

.s-label {
  font-size: 12px;
  font-weight: 700;
  color: var(--sm-stone);
}

.step.done .s-dot {
  background: var(--sm-brand);
  border-color: var(--sm-brand-soft);
}

.step.done .s-label {
  color: var(--sm-ink);
}

.step.now .s-dot {
  background: var(--sm-brand);
  border-color: var(--sm-brand-soft);
  box-shadow: 0 0 0 4px rgba(43, 156, 240, 0.18);
}

.step.now .s-label {
  color: var(--sm-brand-deep);
}

.s-line {
  flex: 1;
  height: 2px;
  margin: 0 8px;
  border-radius: 2px;
  background: var(--sm-line);
  transform: translateY(-10px);

  &.done {
    background: var(--sm-brand);
  }
}

/* 时间线明细 */
.logs {
  margin: 20px 0 0;
  padding: 16px 18px 6px;
  border-radius: 12px;
  background: var(--sm-bg);
  list-style: none;
}

.log {
  display: flex;
  align-items: baseline;
  gap: 12px;
  padding: 5px 0;

  .l-time {
    flex-shrink: 0;
    font-family: var(--sm-font-num);
    font-size: 11.5px;
    font-weight: 500;
    color: var(--sm-stone);
    font-variant-numeric: tabular-nums;
  }

  .l-status {
    font-size: 12.5px;
    font-weight: 700;
    color: var(--sm-ink);
  }

  .l-remark {
    flex: 1;
    min-width: 0;
    font-size: 12px;
    font-weight: 500;
    color: var(--sm-ink-3);
    text-align: right;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

.empty {
  margin: 0;
  padding: 72px 0;
  text-align: center;
  font-size: 13.5px;
  font-weight: 600;
  color: var(--sm-stone);
}

/* ---------- 响应式 ---------- */
@media (max-width: 860px) {
  .sheet {
    grid-template-columns: minmax(0, 1fr);
  }

  .col.side {
    padding-left: 0;
    border-left: none;
    border-top: 1px solid var(--sm-line-soft);
    padding-top: 18px;
  }

  .steps {
    flex-wrap: wrap;
    gap: 10px 0;
  }

  .s-line {
    min-width: 16px;
  }
}
</style>
