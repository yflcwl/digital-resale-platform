<template>
  <div class="app-container">
    <el-card shadow="never">
      <el-form :inline="true" @submit.prevent>
        <el-form-item>
          <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 150px">
            <el-option label="待处理" value="PENDING" />
            <el-option label="已处理" value="HANDLED" />
            <el-option label="已驳回" value="REJECTED" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="list">
        <el-table-column prop="id" label="ID" width="70" align="center" />
        <el-table-column prop="productId" label="商品ID" width="100" align="center" />
        <el-table-column prop="reason" label="举报原因" width="120" align="center" />
        <el-table-column prop="content" label="说明" min-width="180" show-overflow-tooltip />
        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="statusTagType(scope.row.status)" size="small">{{ statusLabel(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="举报时间" width="170" align="center" />
        <el-table-column prop="handleRemark" label="处理备注" min-width="140" show-overflow-tooltip />
        <el-table-column label="操作" width="110" align="center" fixed="right">
          <template #default="scope">
            <el-button v-if="scope.row.status === 'PENDING'" link type="primary" @click="openHandle(scope.row)">处理</el-button>
            <span v-else style="color: var(--sm-stone)">已处理</span>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>

    <!-- 处理弹窗 -->
    <el-dialog v-model="handleVisible" title="处理举报" width="460px" append-to-body>
      <el-form label-width="90px">
        <el-form-item label="处理结果">
          <el-radio-group v-model="handleForm.status">
            <el-radio value="HANDLED">确认违规</el-radio>
            <el-radio value="REJECTED">驳回举报</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="同时下架商品">
          <el-switch v-model="handleForm.offShelf" :disabled="handleForm.status !== 'HANDLED'" />
          <span class="tip">仅对在售商品生效</span>
        </el-form-item>
        <el-form-item label="处理备注">
          <el-input v-model="handleForm.remark" type="textarea" :rows="2" placeholder="处理说明(可选)" maxlength="500" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleVisible = false">取消</el-button>
        <el-button type="primary" :loading="handling" @click="submitHandle">确认处理</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="AdminReport" lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { pageAdminReportList, handleAdminReport } from '@/api/admin';
import { ElMessage } from 'element-plus';

const loading = ref(false);
const total = ref(0);
const list = ref<any[]>([]);
const handleVisible = ref(false);
const handling = ref(false);

const queryParams = reactive<Record<string, any>>({
  pageNum: 1,
  pageSize: 10,
  status: undefined
});

const handleForm = reactive<{ id?: number; status: string; remark: string; offShelf: boolean }>({
  status: 'HANDLED',
  remark: '',
  offShelf: true
});

const statusLabel = (s: string) => ({ PENDING: '待处理', HANDLED: '已处理', REJECTED: '已驳回' })[s] || s;

const statusTagType = (s: string) => ({ PENDING: 'warning', HANDLED: 'success', REJECTED: 'info' })[s] || 'info';

const getList = async () => {
  loading.value = true;
  try {
    const res = await pageAdminReportList(queryParams);
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

const openHandle = (row: any) => {
  handleForm.id = row.id;
  handleForm.status = 'HANDLED';
  handleForm.remark = '';
  handleForm.offShelf = true;
  handleVisible.value = true;
};

const submitHandle = async () => {
  if (!handleForm.id) return;
  handling.value = true;
  try {
    await handleAdminReport(handleForm.id, {
      status: handleForm.status,
      remark: handleForm.remark,
      offShelf: handleForm.status === 'HANDLED' ? handleForm.offShelf : false
    });
    ElMessage.success('处理完成');
    handleVisible.value = false;
    getList();
  } finally {
    handling.value = false;
  }
};

onMounted(() => {
  getList();
});
</script>

<style lang="scss" scoped>
.tip {
  margin-left: 10px;
  color: var(--sm-stone);
  font-size: 12px;
}
</style>
