<template>
  <div class="app-container">
    <el-card shadow="never">
      <el-form :inline="true" @submit.prevent>
        <el-form-item>
          <el-input v-model="queryParams.keyword" placeholder="商品标题 / 识别机型" clearable style="width: 220px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item>
          <el-select v-model="queryParams.grade" placeholder="全部成色" clearable style="width: 130px">
            <el-option v-for="g in ['S', 'A', 'B', 'C', 'D']" :key="g" :label="g + ' 级'" :value="g" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="list">
        <el-table-column label="商品" min-width="200">
          <template #default="scope">
            <div class="product-cell">
              <el-image :src="scope.row.productImage" fit="cover" class="cell-img" />
              <span class="cell-title">{{ scope.row.productTitle }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="brandModel" label="识别机型" width="150" show-overflow-tooltip />
        <el-table-column label="成色" width="80" align="center">
          <template #default="scope">
            <el-tag type="warning" size="small">{{ scope.row.qualityGrade }} 级</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="建议价区间" width="150" align="center">
          <template #default="scope">¥{{ scope.row.priceMin }} ~ ¥{{ scope.row.priceMax }}</template>
        </el-table-column>
        <el-table-column prop="confidence" label="置信度" width="90" align="center" />
        <el-table-column label="报告状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="scope.row.status === 'VALID' ? 'success' : 'info'" size="small">
              {{ scope.row.status === 'VALID' ? '有效' : '失效' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="质检时间" width="170" align="center" />
        <el-table-column label="操作" width="90" align="center" fixed="right">
          <template #default="scope">
            <el-button link type="primary" @click="openDetail(scope.row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>

    <!-- 报告详情 -->
    <el-dialog v-model="dialogVisible" title="质检报告详情" width="560px" append-to-body>
      <template v-if="current">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="商品" :span="2">{{ current.productTitle }}</el-descriptions-item>
          <el-descriptions-item label="识别机型">{{ current.brand_model || current.brandModel }}</el-descriptions-item>
          <el-descriptions-item label="成色等级">{{ current.quality_grade || current.qualityGrade }} 级</el-descriptions-item>
          <el-descriptions-item label="建议价下限">¥{{ current.price_min ?? current.priceMin }}</el-descriptions-item>
          <el-descriptions-item label="建议价上限">¥{{ current.price_max ?? current.priceMax }}</el-descriptions-item>
          <el-descriptions-item label="模型">{{ current.modelName }}</el-descriptions-item>
          <el-descriptions-item label="置信度">{{ current.confidence }}</el-descriptions-item>
        </el-descriptions>

        <h4 style="margin: 16px 0 10px">缺陷明细</h4>
        <el-empty v-if="!current.defects || current.defects.length === 0" description="未检出明显缺陷" :image-size="50" />
        <el-table v-else :data="current.defects" size="small">
          <el-table-column prop="defectType" label="类型" width="110" />
          <el-table-column prop="position" label="位置" width="90" />
          <el-table-column prop="severity" label="严重度" width="100" />
          <el-table-column prop="description" label="描述" min-width="180" show-overflow-tooltip />
        </el-table>

        <p class="disclaimer">AI 辅助质检,仅供参考</p>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="AdminReport" lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { pageAdminReport, getAdminReport } from '@/api/admin';

const loading = ref(false);
const total = ref(0);
const list = ref<any[]>([]);
const dialogVisible = ref(false);
const current = ref<any>();

const queryParams = reactive<Record<string, any>>({
  pageNum: 1,
  pageSize: 10,
  keyword: undefined,
  grade: undefined
});

const getList = async () => {
  loading.value = true;
  try {
    const res = await pageAdminReport(queryParams);
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

const openDetail = async (row: any) => {
  const res = await getAdminReport(row.id);
  current.value = res.data;
  dialogVisible.value = true;
};

onMounted(() => {
  getList();
});
</script>

<style lang="scss" scoped>
.product-cell {
  display: flex;
  align-items: center;
  gap: 10px;
  .cell-img {
    width: 44px;
    height: 44px;
    border-radius: 4px;
    flex-shrink: 0;
  }
  .cell-title {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}
.disclaimer {
  margin-top: 12px;
  color: var(--sm-ink-3);
  font-size: 12px;
  text-align: right;
}
</style>
