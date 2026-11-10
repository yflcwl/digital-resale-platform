<template>
  <div class="app-container" v-loading="loading">
    <el-button icon="ArrowLeft" @click="goBack" style="margin-bottom: 12px">返回</el-button>

    <template v-if="order">
      <el-card shadow="never">
        <template #header>
          <div class="head-bar">
            <span>订单详情</span>
            <el-tag :type="statusTagType(order.status)">{{ orderStatusDict[order.status] || order.status }}</el-tag>
          </div>
        </template>

        <el-descriptions :column="2" border>
          <el-descriptions-item label="订单号">{{ order.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="下单时间">{{ order.createTime }}</el-descriptions-item>
          <el-descriptions-item label="买家">{{ order.buyerNickname || '-' }}</el-descriptions-item>
          <el-descriptions-item label="卖家">{{ order.sellerNickname || '-' }}</el-descriptions-item>
          <el-descriptions-item label="支付时间">{{ order.payTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="发货时间">{{ order.shipTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="快递信息">
            {{ order.expressCompany ? order.expressCompany + ' ' + order.expressNo : '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="收货时间">{{ order.receiveTime || '-' }}</el-descriptions-item>
        </el-descriptions>

        <el-divider />

        <div class="product-row" @click="goProduct">
          <el-image :src="order.productImage" fit="cover" class="product-cover">
            <template #error><div class="cover-slot"><el-icon><picture /></el-icon></div></template>
          </el-image>
          <div class="product-info">
            <div class="product-title">{{ order.productTitle }}</div>
            <div class="product-price">¥{{ order.price }}</div>
          </div>
        </div>

        <el-divider />

        <h4>收货地址</h4>
        <p class="address-text">{{ addressText }}</p>

        <template v-if="canPay || canCancel || canReceive || canReview || canShip">
          <el-divider />
          <div class="actions">
            <el-button v-if="canPay" type="primary" @click="goPay">去支付</el-button>
            <el-button v-if="canCancel" @click="handleCancel">取消订单</el-button>
            <el-button v-if="canReceive" type="success" @click="handleReceive">确认收货</el-button>
            <el-button v-if="canReview" type="warning" @click="openReview">评价</el-button>
            <el-button v-if="canShip" type="primary" @click="openShip">去发货</el-button>
          </div>
        </template>
      </el-card>

      <el-card shadow="never" style="margin-top: 14px">
        <template #header><span>订单流转</span></template>
        <el-timeline>
          <el-timeline-item v-for="log in order.statusLogs || []" :key="log.id" :timestamp="log.createTime" placement="top">
            <span class="log-to">{{ orderStatusDict[log.toStatus] || log.toStatus }}</span>
            <span class="log-remark">{{ log.remark }}</span>
          </el-timeline-item>
        </el-timeline>
      </el-card>
    </template>

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
import { computed, onMounted, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { getOrder, cancelOrder, receiveOrder, shipOrder, reviewOrder } from '@/api/trade/order';
import { OrderVO, orderStatusDict } from '@/api/trade/types';
import { useUserStore } from '@/store/modules/user';
import { ElMessage, ElMessageBox } from 'element-plus';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();
const loading = ref(false);
const order = ref<OrderVO>();

const myId = computed(() => userStore.userId);
const isBuyer = computed(() => order.value && Number(order.value.buyerId) === Number(myId.value));
const isSeller = computed(() => order.value && Number(order.value.sellerId) === Number(myId.value));

const canPay = computed(() => isBuyer.value && order.value?.status === 'PENDING_PAY');
const canCancel = computed(() => isBuyer.value && order.value?.status === 'PENDING_PAY');
const canReceive = computed(() => isBuyer.value && order.value?.status === 'SHIPPED');
const canReview = computed(() => isBuyer.value && order.value?.status === 'RECEIVED');
const canShip = computed(() => isSeller.value && order.value?.status === 'PAID');

const addressText = computed(() => {
  if (!order.value?.addressSnapshot) return '-';
  try {
    const a = JSON.parse(order.value.addressSnapshot);
    return `${a.receiver} ${a.phone} ${a.fullAddress}`;
  } catch {
    return order.value.addressSnapshot;
  }
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

const statusTagType = (status: string) => {
  const map: Record<string, string> = {
    PENDING_PAY: 'warning',
    PAID: 'primary',
    SHIPPED: 'primary',
    RECEIVED: 'success',
    REVIEWED: 'success',
    FINISHED: 'success',
    CANCELED: 'info'
  };
  return map[status] || 'info';
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
  await ElMessageBox.confirm('确认取消该订单?', '提示', { type: 'warning' });
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
.head-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.product-row {
  display: flex;
  gap: 14px;
  cursor: pointer;
  .product-cover {
    width: 90px;
    height: 90px;
    border-radius: 6px;
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
  .product-title {
    font-size: 15px;
    font-weight: 500;
  }
  .product-price {
    margin-top: 8px;
    color: #f56c6c;
    font-size: 18px;
    font-weight: 600;
  }
}
.address-text {
  color: #606266;
}
.actions {
  display: flex;
  gap: 10px;
  justify-content: flex-end;
}
.log-to {
  font-weight: 600;
}
.log-remark {
  margin-left: 10px;
  color: #909399;
}
</style>
