<template>
  <div class="app-container">
    <el-card shadow="never">
      <template #header><span>我的收藏</span></template>
      <el-row :gutter="16" v-loading="loading">
        <el-col :xs="12" :sm="8" :md="6" :lg="4" v-for="item in productList" :key="item.id" class="product-col">
          <el-card shadow="hover" class="product-card" @click="goDetail(item)">
            <div class="product-cover">
              <el-image :src="getCover(item)" fit="cover" class="cover-img" />
            </div>
            <div class="product-title">{{ item.title }}</div>
            <div class="product-price">¥{{ item.price }}</div>
            <div class="product-meta">
              <el-tag v-if="item.status === 'ON_SALE'" type="success" size="small">在售</el-tag>
              <el-tag v-else type="info" size="small">{{ productStatusDict[item.status] || item.status }}</el-tag>
              <el-button link type="danger" size="small" @click.stop="handleCancel(item)">取消收藏</el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-empty v-if="!loading && productList.length === 0" description="暂无收藏" />

      <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>
  </div>
</template>

<script setup name="Favorite" lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { listFavoriteProduct, cancelFavoriteProduct } from '@/api/trade/product';
import { productStatusDict, ProductVO } from '@/api/trade/types';
import { ElMessage } from 'element-plus';

const router = useRouter();
const loading = ref(false);
const total = ref(0);
const productList = ref<ProductVO[]>([]);

const queryParams = reactive({
  pageNum: 1,
  pageSize: 12
});

const getList = async () => {
  loading.value = true;
  try {
    const res = await listFavoriteProduct(queryParams);
    productList.value = res.rows;
    total.value = res.total;
  } finally {
    loading.value = false;
  }
};

const getCover = (item: ProductVO) => {
  return item.images && item.images.length > 0 ? item.images[0].url : '';
};

const goDetail = (item: ProductVO) => {
  router.push('/trade/detail?id=' + item.id);
};

const handleCancel = async (item: ProductVO) => {
  await cancelFavoriteProduct(item.id);
  ElMessage.success('已取消收藏');
  getList();
};

onMounted(() => {
  getList();
});
</script>

<style lang="scss" scoped>
.product-col {
  margin-bottom: 16px;
}
.product-card {
  cursor: pointer;
  .product-cover {
    height: 150px;
    .cover-img {
      width: 100%;
      height: 100%;
    }
  }
  .product-title {
    margin-top: 10px;
    font-size: 14px;
    height: 40px;
    overflow: hidden;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
  }
  .product-price {
    margin-top: 8px;
    color: var(--sm-danger);
    font-size: 18px;
    font-weight: 600;
  }
  .product-meta {
    margin-top: 8px;
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
}
</style>
