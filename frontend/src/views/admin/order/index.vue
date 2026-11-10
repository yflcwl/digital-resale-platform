<template>
  <div class="app-container">
    <el-card shadow="never">
      <el-form :inline="true" @submit.prevent>
        <el-form-item>
          <el-input v-model="queryParams.orderNo" placeholder="订单号" clearable style="width: 200px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item>
          <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 140px">
            <el-option v-for="s in orderStatusDict" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="list">
        <el-table-column prop="orderNo" label="订单号" width="200" />
        <el-table-column prop="productTitle" label="商品" min-width="180" show-overflow-tooltip />
        <el-table-column prop="price" label="金额" width="100" align="center">
          <template #default="scope"><span style="color: var(--sm-danger)">¥{{ scope.row.price }}</span></template>
        </el-table-column>
        <el-table-column prop="buyerNickname" label="买家" width="110" align="center" />
        <el-table-column prop="sellerNickname" label="卖家" width="110" align="center" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="statusTagType(scope.row.status)" size="small">{{ statusLabel(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="下单时间" width="170" align="center" />
        <el-table-column label="操作" width="100" align="center" fixed="right">
          <template #default="scope">
            <el-button link type="primary" @click="openDetail(scope.row)">详情</el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>

    <!-- 订单详情抽屉 -->
    <el-drawer v-model="drawerVisible" title="订单详情" size="480px">
      <template v-if="current">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="订单号">{{ current.orderNo }}</el-descriptions-item>
          <el-descriptions-item label="商品">{{ current.productTitle }}</el-descriptions-item>
          <el-descriptions-item label="金额">¥{{ current.price }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="statusTagType(current.status)" size="small">{{ statusLabel(current.status) }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="买家">{{ current.buyerNickname }}</el-descriptions-item>
          <el-descriptions-item label="卖家">{{ current.sellerNickname }}</el-descriptions-item>
          <el-descriptions-item label="快递">
            {{ current.expressCompany ? current.expressCompany + ' ' + current.expressNo : '-' }}
          </el-descriptions-item>
        </el-descriptions>

        <h4 style="margin-top: 18px">状态流转</h4>
        <el-timeline>
          <el-timeline-item v-for="log in current.statusLogs || []" :key="log.id" :timestamp="log.createTime" placement="top">
            {{ statusLabel(log.toStatus) }} - {{ log.remark }}
          </el-timeline-item>
        </el-timeline>
      </template>
    </el-drawer>
  </div>
</template>

<script setup name="AdminOrder" lang="ts">
import { getCurrentInstance, onMounted, reactive, ref, toRefs } from 'vue';
import { pageAdminOrder, getAdminOrder } from '@/api/admin';
import { OrderVO } from '@/api/trade/types';

const { proxy } = getCurrentInstance() as any;
const { order_status: orderStatusDict } = toRefs<any>(proxy?.useDict('order_status'));

const statusLabel = (v: string) => orderStatusDict.value?.find((d: any) => d.value === v)?.label || v;
import { ElMessage } from 'element-plus';

const loading = ref(false);
const total = ref(0);
const list = ref<OrderVO[]>([]);
const drawerVisible = ref(false);
const current = ref<OrderVO>();

const queryParams = reactive<Record<string, any>>({
  pageNum: 1,
  pageSize: 10,
  orderNo: undefined,
  status: undefined
});

const getList = async () => {
  loading.value = true;
  try {
    const res = await pageAdminOrder(queryParams);
    list.value = res.rows;
    total.value = res.total;
  } finally {
    loading.value = false;
  }
};

const handleQuery = () => {
  queryParams.pageNum = 1;
  getList();
};

const statusTagType = (status: string) => {
  return orderStatusDict.value?.find((d: any) => d.value === status)?.elTagType || 'info';
};

const openDetail = async (row: OrderVO) => {
  try {
    const res = await getAdminOrder(row.id);
    current.value = res.data;
    drawerVisible.value = true;
  } catch {
    ElMessage.error('加载订单详情失败');
  }
};

onMounted(() => {
  getList();
});
</script>
