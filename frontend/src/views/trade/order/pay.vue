<template>
  <div class="app-container pay-wrap" v-loading="loading">
    <button type="button" class="back" @click="goBack">
      <svg viewBox="0 0 16 16" aria-hidden="true">
        <path d="M13 8H4M7.5 4.5 4 8l3.5 3.5" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" />
      </svg>
      返回
    </button>

    <div v-if="order" class="pay-card">
      <!-- 金额 -->
      <p class="amount-label">应付金额</p>
      <p class="amount"><b>¥</b>{{ formatPrice(order.price) }}</p>
      <p class="order-no">订单号 {{ order.orderNo }}</p>

      <!-- 商品行 -->
      <div class="goods">
        <div class="g-cover">
          <img v-if="order.productImage" :src="order.productImage" alt="" />
          <i v-else class="g-letter">{{ (order.productTitle || '·').slice(0, 1) }}</i>
        </div>
        <p class="g-title">{{ order.productTitle }}</p>
      </div>

      <!-- 支付方式 -->
      <div class="methods" role="radiogroup" aria-label="支付方式">
        <button
          type="button"
          :class="['method', { on: payType === 'ALIPAY' }]"
          role="radio"
          :aria-checked="payType === 'ALIPAY'"
          @click="payType = 'ALIPAY'"
        >
          <span class="m-mark m-alipay">支</span>
          <span class="m-name">支付宝<span class="m-sub">(模拟)</span></span>
          <span class="m-check"></span>
        </button>

        <button
          type="button"
          :class="['method', { on: payType === 'WECHAT' }]"
          role="radio"
          :aria-checked="payType === 'WECHAT'"
          @click="payType = 'WECHAT'"
        >
          <span class="m-mark m-wechat">微</span>
          <span class="m-name">微信支付<span class="m-sub">(模拟)</span></span>
          <span class="m-check"></span>
        </button>
      </div>

      <p class="tip">毕业设计演示环境：点击支付即模拟支付成功，不产生真实扣款。</p>

      <button type="button" class="confirm" :disabled="paying" @click="confirmPay">
        <span v-if="paying" class="spin"></span>
        {{ paying ? '支付中…' : `确认支付 ¥${formatPrice(order.price)}` }}
      </button>
    </div>

    <p v-else-if="!loading" class="empty">订单不存在或已被删除。</p>
  </div>
</template>

<script setup name="OrderPay" lang="ts">
import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { getOrder, payOrder } from '@/api/trade/order';
import { OrderVO } from '@/api/trade/types';
import { ElMessage } from 'element-plus';

const route = useRoute();
const router = useRouter();
const loading = ref(false);
const paying = ref(false);
const payType = ref('ALIPAY');
const order = ref<OrderVO>();

const formatPrice = (v?: number) => (v == null ? '—' : Number(v).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 }));

const load = async () => {
  loading.value = true;
  try {
    const res = await getOrder(route.query.id as string);
    order.value = res.data;
  } finally {
    loading.value = false;
  }
};

const confirmPay = async () => {
  if (!order.value) return;
  paying.value = true;
  try {
    await payOrder(order.value.id, payType.value);
    ElMessage.success('支付成功');
    router.push('/trade/order');
  } finally {
    paying.value = false;
  }
};

const goBack = () => {
  router.back();
};

onMounted(() => {
  load();
});
</script>

<style lang="scss" scoped>
.pay-wrap {
  max-width: 560px;
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

/* ---------- 支付卡 ---------- */
.pay-card {
  padding: 32px 34px 26px;
  border: 1px solid var(--sm-card-border);
  border-radius: 24px;
  background: var(--sm-paper);
}

.amount-label {
  margin: 0;
  text-align: center;
  font-size: 12.5px;
  font-weight: 600;
  color: var(--sm-stone);
}

.amount {
  margin: 10px 0 0;
  text-align: center;
  font-family: var(--sm-font-num);
  font-size: 44px;
  font-weight: 800;
  letter-spacing: -0.03em;
  line-height: 1.1;
  color: var(--sm-ink);
  font-variant-numeric: tabular-nums;

  b {
    margin-right: 3px;
    font-size: 22px;
    font-weight: 700;
    color: var(--sm-stone);
  }
}

.order-no {
  margin: 10px 0 0;
  text-align: center;
  font-family: var(--sm-font-num);
  font-size: 11.5px;
  font-weight: 600;
  color: var(--sm-stone);
}

/* 商品行 */
.goods {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 24px;
  padding: 13px 15px;
  border: 1px solid var(--sm-line-soft);
  border-radius: 14px;
  background: var(--sm-bg);
}

.g-cover {
  flex: 0 0 46px;
  width: 46px;
  height: 46px;
  border-radius: 10px;
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
  font-size: 15px;
  font-weight: 800;
  color: var(--sm-brand-deep);
  opacity: 0.4;
}

.g-title {
  flex: 1;
  min-width: 0;
  margin: 0;
  font-size: 13px;
  font-weight: 700;
  color: var(--sm-ink);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 支付方式：两张可选中卡 */
.methods {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
  margin-top: 24px;
}

.method {
  position: relative;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 14px 15px;
  border: 1.5px solid var(--sm-line);
  border-radius: 14px;
  background: var(--sm-paper);
  font-family: inherit;
  cursor: pointer;
  text-align: left;
  transition:
    border-color 0.16s ease,
    background-color 0.16s ease;

  &:hover {
    border-color: var(--sm-line);
  }

  &.on {
    border-color: var(--sm-brand);
    background: var(--sm-brand-tint);
  }
}

.m-mark {
  flex-shrink: 0;
  width: 34px;
  height: 34px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 15px;
  font-weight: 800;
  color: #fff;

  &.m-alipay {
    background: #1677ff;
  }

  &.m-wechat {
    background: #22a06b;
  }
}

.m-name {
  flex: 1;
  min-width: 0;
  font-size: 13.5px;
  font-weight: 800;
  color: var(--sm-ink);

  .m-sub {
    font-size: 11px;
    font-weight: 600;
    color: var(--sm-stone);
  }
}

/* 选中圆点 */
.m-check {
  flex-shrink: 0;
  width: 16px;
  height: 16px;
  border-radius: 50%;
  border: 1.5px solid var(--sm-line);
  transition:
    border-color 0.16s ease,
    box-shadow 0.16s ease;

  .method.on & {
    border-color: var(--sm-brand);
    box-shadow: inset 0 0 0 4px var(--sm-brand);
  }
}

.tip {
  margin: 16px 0 0;
  font-size: 11.5px;
  font-weight: 500;
  line-height: 1.7;
  color: var(--sm-stone);
}

/* 确认按钮：全宽实体键 */
.confirm {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 9px;
  width: 100%;
  height: 48px;
  margin-top: 20px;
  border: none;
  border-radius: var(--sm-r-pill);
  background: var(--sm-brand);
  color: #fff;
  font-family: inherit;
  font-size: 15px;
  font-weight: 800;
  letter-spacing: 0.01em;
  cursor: pointer;
  box-shadow: 0 3px 0 var(--sm-brand-deep);
  transition:
    transform 0.16s var(--sm-ease),
    box-shadow 0.16s var(--sm-ease),
    background-color 0.16s ease,
    opacity 0.16s ease;

  &:hover:not(:disabled) {
    background: #3aa6f2;
  }

  &:active:not(:disabled) {
    transform: translateY(3px);
    box-shadow: 0 0 0 var(--sm-brand-deep);
  }

  &:disabled {
    opacity: 0.7;
    cursor: wait;
  }
}

.spin {
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255, 255, 255, 0.4);
  border-top-color: #fff;
  border-radius: 50%;
  animation: pay-spin 0.7s linear infinite;
}

@keyframes pay-spin {
  to {
    transform: rotate(360deg);
  }
}

@media (prefers-reduced-motion: reduce) {
  .spin {
    animation: none;
  }

  .confirm,
  .method {
    transition: none;
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
</style>
