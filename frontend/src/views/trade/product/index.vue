<template>
  <div class="app-container market-container">
    <!-- 搜索区 -->
    <el-card shadow="never" class="search-card">
      <el-form :inline="true" :model="queryParams" @submit.prevent>
        <el-form-item>
          <el-input v-model="queryParams.title" placeholder="搜索商品关键词" clearable style="width: 220px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item>
          <el-cascader
            v-model="categoryValue"
            :options="categoryOptions"
            :props="cascaderProps"
            placeholder="全部分类"
            clearable
            style="width: 220px"
            @change="handleCategoryChange"
          />
        </el-form-item>
        <el-form-item>
          <el-select v-model="queryParams.brandName" placeholder="全部品牌" clearable style="width: 150px">
            <el-option v-for="b in brandDict" :key="b.value" :label="b.label" :value="b.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-select v-model="queryParams.conditionSelf" placeholder="全部成色" clearable style="width: 150px">
            <el-option v-for="c in conditionDict" :key="c.value" :label="c.label" :value="c.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-input-number v-model="queryParams.priceMin" :min="0" :controls="false" placeholder="最低价" style="width: 110px" />
        </el-form-item>
        <el-form-item>
          <span style="margin: 0 6px">-</span>
          <el-input-number v-model="queryParams.priceMax" :min="0" :controls="false" placeholder="最高价" style="width: 110px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">搜索</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 排序 -->
    <div class="sort-bar">
      <el-radio-group v-model="orderBy" @change="handleQuery">
        <el-radio-button value="">最新发布</el-radio-button>
        <el-radio-button value="asc">价格从低到高</el-radio-button>
        <el-radio-button value="desc">价格从高到低</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 商品卡片 -->
    <el-row :gutter="16" v-loading="loading">
      <el-col :xs="12" :sm="8" :md="6" :lg="4" v-for="item in productList" :key="item.id" class="product-col">
        <el-card shadow="hover" class="product-card" @click="goDetail(item)">
          <div class="product-cover">
            <el-image :src="getCover(item)" fit="cover" class="cover-img">
              <template #error>
                <div class="image-slot"><el-icon><picture /></el-icon></div>
              </template>
            </el-image>
            <el-tag v-if="item.qualityGrade" size="small" class="grade-tag" type="warning">{{ item.qualityGrade }}级</el-tag>
          </div>
          <div class="product-title">{{ item.title }}</div>
          <div class="product-price">
            <span class="price-symbol">¥</span>{{ item.price }}
          </div>
          <div class="product-meta">
            <span>{{ item.categoryName }}</span>
            <span class="view-count">{{ item.viewCount }} 浏览</span>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-empty v-if="!loading && productList.length === 0" description="暂无商品" />

    <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
  </div>
</template>

<script setup name="Market" lang="ts">
import { getCurrentInstance, onMounted, reactive, ref, toRefs } from 'vue';
import { useRouter } from 'vue-router';
import { listCategoryTree } from '@/api/trade/category';
import { listOnSaleProduct } from '@/api/trade/product';
import { getDicts } from '@/api/system/dict/data';
import { ProductQuery, ProductVO, CategoryTreeVO } from '@/api/trade/types';

const { proxy } = getCurrentInstance() as any;
const { product_condition: conditionDict } = toRefs<any>(proxy?.useDict('product_condition'));

const router = useRouter();
const loading = ref(false);
const total = ref(0);
const productList = ref<ProductVO[]>([]);
const categoryOptions = ref<CategoryTreeVO[]>([]);
const brandDict = ref<{ label: string; value: string }[]>([]);
const categoryValue = ref<number[]>([]);
const orderBy = ref('');
const cascaderProps = { value: 'id', label: 'name', checkStrictly: true, emitPath: true };

const queryParams = reactive<ProductQuery>({
  pageNum: 1,
  pageSize: 20
});

const getList = async () => {
  loading.value = true;
  try {
    const params: ProductQuery = { ...queryParams };
    if (orderBy.value) {
      params.orderByColumn = 'price';
      params.isAsc = orderBy.value;
    }
    const res = await listOnSaleProduct(params);
    productList.value = res.rows;
    total.value = res.total;
  } finally {
    loading.value = false;
  }
};

const loadCategory = async () => {
  const res = await listCategoryTree();
  categoryOptions.value = res.data;
};

/** 品牌字典(匿名访问字典接口可能 401,失败时降级隐藏品牌筛选) */
const loadBrandDict = async () => {
  try {
    const res = await getDicts('product_brand');
    brandDict.value = res.data.map((d: any) => ({ label: d.dictLabel, value: d.dictValue }));
  } catch {
    brandDict.value = [];
  }
};

const handleCategoryChange = () => {
  const val = categoryValue.value;
  queryParams.categoryId = val && val.length ? val[val.length - 1] : undefined;
  queryParams.brandName = undefined;
  handleQuery();
};

const handleQuery = () => {
  queryParams.pageNum = 1;
  getList();
};

const resetQuery = () => {
  queryParams.title = undefined;
  queryParams.categoryId = undefined;
  queryParams.brandName = undefined;
  queryParams.conditionSelf = undefined;
  queryParams.priceMin = undefined;
  queryParams.priceMax = undefined;
  categoryValue.value = [];
  orderBy.value = '';
  handleQuery();
};

const getCover = (item: ProductVO) => {
  if (item.images && item.images.length > 0) {
    return item.images[0].url;
  }
  return '';
};

const goDetail = (item: ProductVO) => {
  router.push('/trade/detail?id=' + item.id);
};

onMounted(() => {
  loadCategory();
  loadBrandDict();
  getList();
});
</script>

<style lang="scss" scoped>
.market-container {
  .search-card {
    margin-bottom: 12px;
  }
  .sort-bar {
    margin-bottom: 12px;
  }
  .product-col {
    margin-bottom: 16px;
  }
  .product-card {
    cursor: pointer;
    .product-cover {
      position: relative;
      height: 160px;
      .cover-img {
        width: 100%;
        height: 100%;
      }
      .image-slot {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 100%;
        height: 100%;
        background: var(--sm-line-soft);
        color: var(--sm-ink-3);
        font-size: 28px;
      }
      .grade-tag {
        position: absolute;
        top: 6px;
        right: 6px;
      }
    }
    .product-title {
      margin-top: 10px;
      font-size: 14px;
      line-height: 20px;
      height: 40px;
      overflow: hidden;
      display: -webkit-box;
      -webkit-line-clamp: 2;
      -webkit-box-orient: vertical;
    }
    .product-price {
      margin-top: 8px;
      color: var(--sm-danger);
      font-size: 20px;
      font-weight: 600;
      .price-symbol {
        font-size: 13px;
      }
    }
    .product-meta {
      margin-top: 6px;
      display: flex;
      justify-content: space-between;
      color: var(--sm-ink-3);
      font-size: 12px;
    }
  }
}
</style>
