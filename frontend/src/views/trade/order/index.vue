<template>
  <div class="app-container orders">
    <header class="sm-page-head">
      <div>
        <h1 class="sm-page-title">我的订单</h1>
        <p class="sm-page-desc">下单、支付、发货、收货都在这里推进。</p>
      </div>
    </header>

    <div class="bar">
      <div class="seg">
        <button type="button" :class="['seg-btn', { on: role === 'buyer' }]" @click="switchRole('buyer')">我买到的</button>
        <button type="button" :class="['seg-btn', { on: role === 'seller' }]" @click="switchRole('seller')">我卖出的</button>
      </div>

      <el-select v-model="queryParams.status" placeholder="全部状态" clearable class="status-select" @change="handleQuery">
        <el-option v-for="s in orderStatusDict" :key="s.value" :label="s.label" :value="s.value" />
      </el-select>
    </div>

    <div v-loading="loading" class="list">
      <article v-for="o in list" :key="o.id" class="order">
        <header class="o-head">
          <span class="o-no">{{ o.orderNo }}</span>
          <span class="o-time">{{ o.createTime }}</span>
          <span :class="['o-status', statusClass(o.status)]">{{ statusLabel(o.status) }}</span>
        </header>

        <div class="o-body" @click="goDetail(o)">
          <div class="o-cover">
            <img v-if="o.productImage" :src="o.productImage" alt="" loading="lazy" />
            <i v-else class="o-letter">{{ (o.productTitle || '·').slice(0, 1) }}</i>
          </div>

          <div class="o-main">
            <p class="o-title">{{ o.productTitle }}</p>
            <p class="o-parties">
              <span>买家 {{ o.buyerNickname || '—' }}</span>
              <i></i>
              <span>卖家 {{ o.sellerNickname || '—' }}</span>
            </p>
          </div>

          <p class="o-price"><b>¥</b>{{ formatPrice(o.price) }}</p>
        </div>

        <footer class="o-foot">
          <button type="button" class="act-text" @click="goDetail(o)">订单详情</button>
          <div class="o-actions">
            <template v-if="role === 'buyer'">
              <button v-if="o.status === 'PENDING_PAY'" type="button" class="act-ghost" @click="handleCancel(o)">取消订单</button>
              <button v-if="o.status === 'PENDING_PAY'" type="button" class="act-primary" @click="goPay(o)">去支付</button>
              <button v-if="o.status === 'SHIPPED'" type="button" class="act-primary" @click="handleReceive(o)">确认收货</button>
              <button v-if="o.status === 'RECEIVED'" type="button" class="act-primary" @click="openReview(o)">评价</button>
            </template>
            <template v-else>
              <button v-if="o.status === 'PAID'" type="button" class="act-primary" @click="openShip(o)">去发货</button>
            </template>
          </div>
        </footer>
      </article>

      <p v-if="!loading && list.length === 0" class="empty">
        {{ role === 'buyer' ? '还没有买过东西。' : '还没有卖出过东西。' }}
      </p>
    </div>

    <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />

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
import { ElMessage, ElMessageBox } from 'element-plus';

const { proxy } = getCurrentInstance() as any;
const { order_status: orderStatusDict } = toRefs<any>(proxy?.useDict('order_status'));

const statusLabel = (v: string) => orderStatusDict.value?.find((d: any) => d.value === v)?.label || v;

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

const formatPrice = (v?: number) => (v == null ? '—' : Number(v).toLocaleString('zh-CN'));

/** 状态用语义色区分，而不是一水儿的灰标签 */
const statusClass = (status: string) => {
  if (status === 'PENDING_PAY') return 'is-wait';
  if (status === 'PAID' || status === 'SHIPPED') return 'is-going';
  if (status === 'RECEIVED' || status === 'COMPLETED') return 'is-done';
  return 'is-idle';
};

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

const switchRole = (r: string) => {
  role.value = r;
  queryParams.pageNum = 1;
  queryParams.status = undefined;
  getList();
};

const handleQuery = () => {
  queryParams.pageNum = 1;
  getList();
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
.orders {
  max-width: 1080px;
}

/* ---------- 顶部：分段切换 + 状态筛选 ---------- */
.bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 16px;
}

.seg {
  display: inline-flex;
  gap: 3px;
  padding: 4px;
  border-radius: var(--sm-r-pill);
  background: var(--sm-bg-2);
}

.seg-btn {
  padding: 7px 18px;
  border: none;
  border-radius: var(--sm-r-pill);
  background: transparent;
  font-family: inherit;
  font-size: 13px;
  font-weight: 700;
  color: var(--sm-ink-2);
  cursor: pointer;
  transition:
    background-color var(--sm-dur) ease,
    color var(--sm-dur) ease;

  &.on {
    background: var(--sm-paper);
    color: var(--sm-ink);
    box-shadow: 0 1px 3px rgba(16, 45, 80, 0.14);
  }
}

.status-select {
  width: 150px;
}

/* ---------- 订单行 ---------- */
.list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 120px;
}

.order {
  padding: 16px 20px 14px;
  border: 1px solid var(--sm-card-border);
  border-radius: 18px;
  background: var(--sm-paper);
  transition:
    border-color 0.18s ease,
    box-shadow 0.18s ease;

  &:hover {
    border-color: var(--sm-line);
    box-shadow: 0 6px 20px -12px rgba(16, 45, 80, 0.24);
  }
}

.o-head {
  display: flex;
  align-items: center;
  gap: 14px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--sm-line-soft);
}

.o-no {
  font-family: var(--sm-font-num);
  font-size: 12.5px;
  font-weight: 700;
  color: var(--sm-ink-2);
  letter-spacing: 0.01em;
}

.o-time {
  flex: 1;
  font-size: 11.5px;
  font-weight: 500;
  color: var(--sm-stone);
}

.o-status {
  padding: 3px 10px;
  border-radius: var(--sm-r-pill);
  font-size: 11.5px;
  font-weight: 700;

  &.is-wait {
    background: #fdf0dc;
    color: #9a6300;
  }

  &.is-going {
    background: var(--sm-brand-soft);
    color: var(--sm-brand-deep);
  }

  &.is-done {
    background: var(--sm-accent-soft);
    color: #1c7a52;
  }

  &.is-idle {
    background: var(--sm-bg-2);
    color: var(--sm-ink-3);
  }
}

.o-body {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 0;
  cursor: pointer;
}

.o-cover {
  flex: 0 0 72px;
  width: 72px;
  height: 72px;
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

.o-letter {
  font-style: normal;
  font-size: 20px;
  font-weight: 800;
  color: var(--sm-brand-deep);
  opacity: 0.4;
}

.o-main {
  flex: 1;
  min-width: 0;
}

.o-title {
  margin: 0;
  font-size: 14px;
  font-weight: 700;
  line-height: 1.5;
  color: var(--sm-ink);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.o-parties {
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 6px 0 0;
  font-size: 11.5px;
  font-weight: 500;
  color: var(--sm-stone);

  i {
    width: 3px;
    height: 3px;
    border-radius: 50%;
    background: var(--sm-line);
  }
}

.o-price {
  flex: 0 0 auto;
  margin: 0;
  padding-left: 12px;
  font-family: var(--sm-font-num);
  font-size: 22px;
  font-weight: 800;
  letter-spacing: -0.02em;
  color: var(--sm-ink);
  font-variant-numeric: tabular-nums;

  b {
    margin-right: 1px;
    font-size: 13px;
    font-weight: 700;
    color: var(--sm-stone);
  }
}

.o-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding-top: 12px;
  border-top: 1px solid var(--sm-line-soft);
}

.o-actions {
  display: flex;
  align-items: center;
  gap: 8px;
}

.act-text,
.act-ghost,
.act-primary {
  font-family: inherit;
  cursor: pointer;
  transition:
    background-color 0.16s ease,
    border-color 0.16s ease,
    color 0.16s ease,
    transform 0.16s var(--sm-ease),
    box-shadow 0.16s var(--sm-ease);
}

.act-text {
  padding: 0;
  border: none;
  background: none;
  font-size: 12.5px;
  font-weight: 600;
  color: var(--sm-ink-3);

  &:hover {
    color: var(--sm-brand-deep);
  }
}

.act-ghost {
  height: 32px;
  padding: 0 14px;
  border: 1px solid var(--sm-line);
  border-radius: var(--sm-r-pill);
  background: var(--sm-paper);
  font-size: 12.5px;
  font-weight: 700;
  color: var(--sm-ink-2);

  &:hover {
    border-color: var(--sm-brand);
    color: var(--sm-brand-deep);
  }
}

.act-primary {
  height: 32px;
  padding: 0 16px;
  border: none;
  border-radius: var(--sm-r-pill);
  background: var(--sm-brand);
  color: #fff;
  font-size: 12.5px;
  font-weight: 700;
  box-shadow: 0 2px 0 var(--sm-brand-deep);

  &:hover {
    background: #3aa6f2;
  }

  &:active {
    transform: translateY(2px);
    box-shadow: 0 0 0 var(--sm-brand-deep);
  }
}

.empty {
  margin: 0;
  padding: 60px 0;
  text-align: center;
  font-size: 13.5px;
  font-weight: 600;
  color: var(--sm-stone);
}

@media (max-width: 700px) {
  .o-price {
    font-size: 18px;
  }

  .o-cover {
    flex-basis: 60px;
    width: 60px;
    height: 60px;
  }

  .o-foot {
    flex-wrap: wrap;
  }
}
</style>
