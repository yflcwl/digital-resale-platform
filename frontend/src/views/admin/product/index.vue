<template>
  <div class="app-container">
    <el-card shadow="never">
      <el-form :inline="true" @submit.prevent>
        <el-form-item>
          <el-input v-model="queryParams.title" placeholder="商品标题" clearable style="width: 200px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item>
          <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 140px">
            <el-option v-for="s in productStatusOptions" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
        </el-form-item>
      </el-form>

      <div class="batch-bar">
        <el-button type="primary" plain :disabled="selectedIds.length === 0" @click="handleSetFeatured(true)">加入首页精选</el-button>
        <el-button plain :disabled="selectedIds.length === 0" @click="handleSetFeatured(false)">取消精选</el-button>
        <span class="batch-tip">已选 {{ selectedIds.length }} 件(精选仅支持在售商品)</span>
      </div>

      <el-table v-loading="loading" :data="list" @selection-change="handleSelectionChange">
        <el-table-column type="selection" width="46" align="center" />
        <el-table-column label="图片" width="80" align="center">
          <template #default="scope">
            <el-image :src="cover(scope.row)" fit="cover" style="width: 50px; height: 50px; border-radius: 4px" />
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="categoryName" label="分类" width="100" align="center" />
        <el-table-column prop="price" label="价格" width="100" align="center">
          <template #default="scope"><span style="color: var(--sm-danger)">¥{{ scope.row.price }}</span></template>
        </el-table-column>
        <el-table-column label="成色" width="80" align="center">
          <template #default="scope">{{ scope.row.qualityGrade || '-' }}</template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="tagType(scope.row.status)" size="small">{{ productStatusDict[scope.row.status] || scope.row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="精选" width="80" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.isFeatured === '1'" type="danger" size="small" effect="plain">精选</el-tag>
            <span v-else style="color: var(--sm-stone)">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="sellerId" label="卖家ID" width="90" align="center" />
        <el-table-column prop="createTime" label="发布时间" width="170" align="center" />
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="scope">
            <el-button v-if="scope.row.status === 'ON_SALE'" link type="warning" @click="handleOffShelf(scope.row)">强制下架</el-button>
            <el-button v-if="['DRAFT', 'OFF_SHELF'].includes(scope.row.status)" link type="danger" @click="handleDelete(scope.row)">删除</el-button>
            <el-button link type="primary" @click="goDetail(scope.row)">查看</el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>
  </div>
</template>

<script setup name="AdminProduct" lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { pageAdminProduct, adminOffShelf, adminDelProduct, adminSetFeatured } from '@/api/admin';
import { productStatusDict, productStatusOptions, ProductVO } from '@/api/trade/types';
import { ElMessage, ElMessageBox } from 'element-plus';

const router = useRouter();
const loading = ref(false);
const total = ref(0);
const list = ref<ProductVO[]>([]);
const selectedIds = ref<number[]>([]);

const queryParams = reactive<Record<string, any>>({
  pageNum: 1,
  pageSize: 10,
  title: undefined,
  status: undefined
});

const handleSelectionChange = (rows: ProductVO[]) => {
  selectedIds.value = rows.map((r) => r.id);
};

const handleSetFeatured = async (featured: boolean) => {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请先选择商品');
    return;
  }
  await adminSetFeatured(selectedIds.value, featured);
  ElMessage.success(featured ? '已加入首页精选' : '已取消精选');
  getList();
};
const getList = async () => {
  loading.value = true;
  try {
    const res = await pageAdminProduct(queryParams);
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

const cover = (row: ProductVO) => (row.images && row.images.length ? row.images[0].url : '');

const tagType = (status: string) => {
  const map: Record<string, string> = {
    DRAFT: 'info',
    WAITING_QC: 'primary',
    ON_SALE: 'success',
    LOCKED: 'warning',
    SOLD: 'danger',
    OFF_SHELF: 'info',
    AUDITING: 'warning',
    REJECTED: 'danger'
  };
  return map[status] || 'info';
};

const handleOffShelf = async (row: ProductVO) => {
  await ElMessageBox.confirm(`确认强制下架「${row.title}」?`, '提示', { type: 'warning' });
  await adminOffShelf(row.id);
  ElMessage.success('已强制下架');
  getList();
};

const handleDelete = async (row: ProductVO) => {
  await ElMessageBox.confirm(`确认删除「${row.title}」?`, '提示', { type: 'warning' });
  await adminDelProduct(row.id);
  ElMessage.success('已删除');
  getList();
};

const goDetail = (row: ProductVO) => {
  router.push('/trade/detail?id=' + row.id);
};

onMounted(() => {
  getList();
});
</script>
