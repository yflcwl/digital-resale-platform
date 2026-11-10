<template>
  <div class="app-container">
    <el-card shadow="never" v-loading="loading">
      <template #header><span>模拟支付</span></template>
      <template v-if="order">
        <div class="pay-summary">
          <el-image :src="order.productImage" fit="cover" class="pay-cover">
            <template #error><div class="cover-slot"><el-icon><picture /></el-icon></div></template>
          </el-image>
          <div class="pay-info">
            <div class="pay-title">{{ order.productTitle }}</div>
            <div class="pay-no">订单号:{{ order.orderNo }}</div>
            <div class="pay-price">应付金额 <span class="amount">¥{{ order.price }}</span></div>
          </div>
        </div>

        <el-divider />

        <div class="pay-methods">
          <h4>选择支付方式</h4>
          <el-radio-group v-model="payType" class="method-group">
            <el-radio value="ALIPAY" border size="large">支付宝(模拟)</el-radio>
            <el-radio value="WECHAT" border size="large">微信支付(模拟)</el-radio>
          </el-radio-group>
          <p class="pay-tip">毕业设计演示环境:点击支付即模拟支付成功,不产生真实扣款</p>
        </div>

        <div class="pay-actions">
          <el-button size="large" @click="goBack">返回</el-button>
          <el-button type="primary" size="large" :loading="paying" @click="confirmPay">确认支付 ¥{{ order.price }}</el-button>
        </div>
      </template>
      <el-empty v-else-if="!loading" description="订单不存在" />
    </el-card>
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
.pay-summary {
  display: flex;
  gap: 16px;
  .pay-cover {
    width: 110px;
    height: 110px;
    border-radius: 8px;
  }
  .cover-slot {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 100%;
    height: 100%;
    background: #f5f7fa;
    color: #909399;
  }
  .pay-title {
    font-size: 16px;
    font-weight: 600;
  }
  .pay-no {
    margin-top: 8px;
    color: #909399;
    font-size: 12px;
  }
  .pay-price {
    margin-top: 14px;
    .amount {
      color: #f56c6c;
      font-size: 26px;
      font-weight: 700;
      margin-left: 8px;
    }
  }
}
.pay-methods {
  .method-group {
    display: flex;
    gap: 14px;
  }
  .pay-tip {
    margin-top: 14px;
    color: #909399;
    font-size: 12px;
  }
}
.pay-actions {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}
</style>
