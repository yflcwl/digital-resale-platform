<template>
  <div class="app-container">
    <el-card shadow="never">
      <template #header>
        <el-tabs v-model="role" @tab-change="handleTabChange">
          <el-tab-pane label="我买到的" name="buyer" />
          <el-tab-pane label="我卖出的" name="seller" />
        </el-tabs>
      </template>

      <el-form :inline="true" @submit.prevent>
        <el-form-item>
          <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 150px;">
            <el-option v-for="s in orderStatusDict" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
        </el-form-item>
      </el-form>

      <el-empty v-if="!loading && list.length === 0" description="暂无订单" />

      <div v-loading="loading">
        <el-card v-for="o in list" :key="o.id" shadow="hover" class="order-card">
          <div class="order-head">
            <span class="order-no">订单号:{{ o.orderNo }}</span>
            <span class="order-time">{{ o.createTime }}</span>
            <el-tag :type="statusTagType(o.status)" size="small">{{ statusLabel(o.status) }}</el-tag>
          </div>
          <div class="order-body" @click="goDetail(o)">
            <el-image :src="o.productImage" fit="cover" class="order-cover">
              <template #error><div class="cover-slot"><el-icon><picture /></el-icon></div></template>
            </el-image>
            <div class="order-info">
              <div class="order-title">{{ o.productTitle }}</div>
              <div class="order-price">¥{{ o.price }}</div>
              <div class="order-parties">
                <span>买家:{{ o.buyerNickname || '-' }}</span>
                <span>卖家:{{ o.sellerNickname || '-' }}</span>
              </div>
            </div>
          </div>
          <div class="order-actions">
            <template v-if="role === 'buyer'">
              <el-button v-if="o.status === 'PENDING_PAY'" type="primary" size="small" @click="goPay(o)">去支付</el-button>
              <el-button v-if="o.status === 'PENDING_PAY'" size="small" @click="handleCancel(o)">取消订单</el-button>
              <el-button v-if="o.status === 'SHIPPED'" type="success" size="small" @click="handleReceive(o)">确认收货</el-button>
              <el-button v-if="o.status === 'RECEIVED'" type="warning" size="small" @click="openReview(o)">评价</el-button>
            </template>
            <template v-else>
              <el-button v-if="o.status === 'PAID'" type="primary" size="small" @click="openShip(o)">去发货</el-button>
            </template>
            <el-button size="small" link type="primary" @click="goDetail(o)">订单详情</el-button>
          </div>
        </el-card>
      </div>

      <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>

    <!-- 发货弹窗 -->
    <el-dialog v-model="shipVisible" title="填写发货信息" width="440px" append-to-body>
      <el-form label-width="90px">
        <el-form-item label="快递公司">
          <el-select v-model="shipForm.expressCompany" placeholder="请选择" style="width: 100%">
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
          <el-input v-model="reviewForm.content" type="textarea" :rows="3" placeholder="说说这次的交易体验(可选)" maxlength="500" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="reviewVisible = false">取消</el-button>
        <el-button type="primary" :loading="reviewing" @click="submitReview">提交评价</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="Order" lang="ts">
import { getCurrentInstance, onMounted, reactive, ref, toRefs } from 'vue';
import { useRouter } from 'vue-router';
import { listMyOrder, cancelOrder, receiveOrder, shipOrder, reviewOrder } from '@/api/trade/order';
import { OrderQuery, OrderVO } from '@/api/trade/types';

const { proxy } = getCurrentInstance() as any;
const { order_status: orderStatusDict } = toRefs<any>(proxy?.useDict('order_status'));

const statusLabel = (v: string) => orderStatusDict.value?.find((d: any) => d.value === v)?.label || v;
import { ElMessage, ElMessageBox } from 'element-plus';

const router = useRouter();
const role = ref('buyer');
const loading = ref(false);
const total = ref(0);
const list = ref<OrderVO[]>([]);

const queryParams = reactive<OrderQuery>({
  pageNum: 1,
  pageSize: 10,
  role: 'buyer'
});

const getList = async () => {
  loading.value = true;
  try {
    queryParams.role = role.value;
    const res = await listMyOrder(queryParams);
    list.value = res.rows;
    total.value = res.total;
  } finally {
    loading.value = false;
  }
};

const handleTabChange = () => {
  queryParams.pageNum = 1;
  queryParams.status = undefined;
  getList();
};

const handleQuery = () => {
  queryParams.pageNum = 1;
  getList();
};

const statusTagType = (status: string) => {
  return orderStatusDict.value?.find((d: any) => d.value === status)?.elTagType || 'info';
};

const goDetail = (o: OrderVO) => {
  router.push('/trade/orderDetail?id=' + o.id);
};

const goPay = (o: OrderVO) => {
  router.push('/trade/pay?id=' + o.id);
};

const handleCancel = async (o: OrderVO) => {
  await ElMessageBox.confirm('确认取消该订单?取消后商品将重新上架。', '提示', { type: 'warning' });
  await cancelOrder(o.id);
  ElMessage.success('订单已取消');
  getList();
};

const handleReceive = async (o: OrderVO) => {
  await ElMessageBox.confirm('确认已收到商品?', '提示', { type: 'warning' });
  await receiveOrder(o.id);
  ElMessage.success('确认收货成功');
  getList();
};

/* 发货 */
const shipVisible = ref(false);
const shipping = ref(false);
const shipForm = reactive({ orderId: 0, expressCompany: '顺丰速运', expressNo: '' });

const openShip = (o: OrderVO) => {
  shipForm.orderId = o.id;
  shipForm.expressNo = '';
  shipVisible.value = true;
};

const submitShip = async () => {
  if (!shipForm.expressNo) {
    ElMessage.warning('请输入快递单号');
    return;
  }
  shipping.value = true;
  try {
    await shipOrder(shipForm.orderId, { expressCompany: shipForm.expressCompany, expressNo: shipForm.expressNo });
    ElMessage.success('发货成功');
    shipVisible.value = false;
    getList();
  } finally {
    shipping.value = false;
  }
};

/* 评价 */
const reviewVisible = ref(false);
const reviewing = ref(false);
const reviewForm = reactive({ orderId: 0, rating: 5, content: '' });

const openReview = (o: OrderVO) => {
  reviewForm.orderId = o.id;
  reviewForm.rating = 5;
  reviewForm.content = '';
  reviewVisible.value = true;
};

const submitReview = async () => {
  reviewing.value = true;
  try {
    await reviewOrder(reviewForm.orderId, { rating: reviewForm.rating, content: reviewForm.content });
    ElMessage.success('评价成功');
    reviewVisible.value = false;
    getList();
  } finally {
    reviewing.value = false;
  }
};

onMounted(() => {
  getList();
});
</script>

<style lang="scss" scoped>
.order-card {
  margin-bottom: 14px;
}
.order-head {
  display: flex;
  align-items: center;
  gap: 16px;
  padding-bottom: 10px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  .order-no {
    font-weight: 600;
  }
  .order-time {
    color: var(--sm-ink-3);
    font-size: 12px;
    flex: 1;
  }
}
.order-body {
  display: flex;
  gap: 14px;
  padding: 12px 0;
  cursor: pointer;
  .order-cover {
    width: 84px;
    height: 84px;
    border-radius: 6px;
    flex-shrink: 0;
  }
  .cover-slot {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 100%;
    height: 100%;
    background: var(--sm-line-soft);
    color: var(--sm-ink-3);
  }
  .order-info {
    flex: 1;
    .order-title {
      font-size: 15px;
      font-weight: 500;
    }
    .order-price {
      margin-top: 8px;
      color: var(--sm-danger);
      font-size: 18px;
      font-weight: 600;
    }
    .order-parties {
      margin-top: 8px;
      color: var(--sm-ink-3);
      font-size: 12px;
      display: flex;
      gap: 18px;
    }
  }
}
.order-actions {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  padding-top: 10px;
  border-top: 1px solid var(--el-border-color-lighter);
}
</style>
