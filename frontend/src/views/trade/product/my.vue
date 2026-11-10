<template>
  <div class="app-container">
    <el-card shadow="never">
      <template #header>
        <div class="header-bar">
          <span>我的商品</span>
          <el-button type="primary" @click="goPublish">发布商品</el-button>
        </div>
      </template>

      <el-form :inline="true" :model="queryParams" @submit.prevent>
        <el-form-item>
          <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 140px">
            <el-option v-for="s in productStatusOptions" :key="s.value" :label="s.label" :value="s.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-input v-model="queryParams.title" placeholder="标题关键词" clearable style="width: 200px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
        </el-form-item>
      </el-form>

      <el-table v-loading="loading" :data="productList">
        <el-table-column label="图片" width="90" align="center">
          <template #default="scope">
            <el-image :src="getCover(scope.row)" fit="cover" style="width: 60px; height: 60px" />
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
        <el-table-column prop="categoryName" label="分类" width="100" align="center" />
        <el-table-column prop="price" label="价格" width="110" align="center">
          <template #default="scope">
            <span style="color: #f56c6c">¥{{ scope.row.price }}</span>
          </template>
        </el-table-column>
        <el-table-column label="成色" width="80" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.qualityGrade" type="warning" size="small">{{ scope.row.qualityGrade }}级</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="statusTagType(scope.row.status)" size="small">{{ productStatusDict[scope.row.status] || scope.row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间" width="170" align="center" />
        <el-table-column label="操作" width="240" align="center" fixed="right">
          <template #default="scope">
            <el-button link type="primary" @click="goDetail(scope.row)">查看</el-button>
            <el-button v-if="['DRAFT', 'OFF_SHELF'].includes(scope.row.status)" link type="primary" @click="goEdit(scope.row)">编辑</el-button>
            <el-button v-if="scope.row.status === 'ON_SALE'" link type="warning" @click="handleOffShelf(scope.row)">下架</el-button>
            <el-button v-if="scope.row.status === 'OFF_SHELF'" link type="success" @click="handleOnShelf(scope.row)">上架</el-button>
            <el-button v-if="['DRAFT', 'OFF_SHELF'].includes(scope.row.status)" link type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>
  </div>
</template>

<script setup name="MyProduct" lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { listMyProduct, offShelfProduct, onShelfProduct, delProduct } from '@/api/trade/product';
import { productStatusDict, productStatusOptions, ProductQuery, ProductVO } from '@/api/trade/types';
import { ElMessage, ElMessageBox } from 'element-plus';

const router = useRouter();
const loading = ref(false);
const total = ref(0);
const productList = ref<ProductVO[]>([]);

const queryParams = reactive<ProductQuery>({
  pageNum: 1,
  pageSize: 10
});

const getList = async () => {
  loading.value = true;
  try {
    const res = await listMyProduct(queryParams);
    productList.value = res.rows;
    total.value = res.total;
  } finally {
    loading.value = false;
  }
};

const handleQuery = () => {
  queryParams.pageNum = 1;
  getList();
};

const getCover = (item: ProductVO) => {
  return item.images && item.images.length > 0 ? item.images[0].url : '';
};

const statusTagType = (status: string) => {
  const map: Record<string, string> = {
    DRAFT: 'info',
    WAITING_QC: 'primary',
    ON_SALE: 'success',
    LOCKED: 'warning',
    SOLD: 'danger',
    OFF_SHELF: 'info'
  };
  return map[status] || 'info';
};

const goPublish = () => {
  router.push('/trade/publish');
};

const goDetail = (row: ProductVO) => {
  router.push('/trade/detail?id=' + row.id);
};

const goEdit = (row: ProductVO) => {
  router.push('/trade/publish?editId=' + row.id);
};

const handleOffShelf = async (row: ProductVO) => {
  await ElMessageBox.confirm('确认下架该商品?', '提示', { type: 'warning' });
  await offShelfProduct(row.id);
  ElMessage.success('已下架');
  getList();
};

const handleOnShelf = async (row: ProductVO) => {
  await ElMessageBox.confirm('确认重新上架该商品?', '提示', { type: 'warning' });
  await onShelfProduct(row.id);
  ElMessage.success('已上架');
  getList();
};

const handleDelete = async (row: ProductVO) => {
  await ElMessageBox.confirm('确认删除该商品?', '提示', { type: 'warning' });
  await delProduct(row.id);
  ElMessage.success('已删除');
  getList();
};

onMounted(() => {
  getList();
});
</script>

<style lang="scss" scoped>
.header-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
