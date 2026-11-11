<template>
  <div class="app-container market">
    <header class="sm-page-head">
      <div>
        <h1 class="sm-page-title">商品市场</h1>
        <p class="sm-page-desc">在售商品都已通过 AI 质检，点开任意一件可以先看成色报告。</p>
      </div>
    </header>

    <!-- 筛选条 -->
    <div class="filter">
      <div class="filter-row">
        <el-input
          v-model="queryParams.title"
          class="f-search"
          placeholder="搜索商品关键词"
          clearable
          @keyup.enter="handleQuery"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>

        <el-cascader
          v-model="categoryValue"
          :options="categoryOptions"
          :props="cascaderProps"
          placeholder="全部分类"
          clearable
          class="f-cat"
          @change="handleCategoryChange"
        />

        <el-select v-model="queryParams.brandName" placeholder="全部品牌" clearable class="f-brand">
          <el-option v-for="b in brandDict" :key="b.value" :label="b.label" :value="b.value" />
        </el-select>

        <el-select v-model="queryParams.conditionSelf" placeholder="全部成色" clearable class="f-cond">
          <el-option v-for="c in conditionDict" :key="c.value" :label="c.label" :value="c.value" />
        </el-select>

        <div class="f-price">
          <el-input-number v-model="queryParams.priceMin" :min="0" :controls="false" placeholder="¥ 最低" />
          <span class="dash">—</span>
          <el-input-number v-model="queryParams.priceMax" :min="0" :controls="false" placeholder="¥ 最高" />
        </div>

        <div class="f-actions">
          <el-button type="primary" @click="handleQuery">搜索</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </div>
      </div>
    </div>

    <!-- 排序 -->
    <div class="sort-bar">
      <el-radio-group v-model="orderBy" @change="handleQuery">
        <el-radio-button value="">最新发布</el-radio-button>
        <el-radio-button value="asc">价格从低到高</el-radio-button>
        <el-radio-button value="desc">价格从高到低</el-radio-button>
      </el-radio-group>
      <span class="sort-count">共 <b>{{ total }}</b> 件在售</span>
    </div>

    <!-- 商品卡 -->
    <div v-loading="loading" class="grid">
      <article
        v-for="item in productList"
        :key="item.id"
        class="good"
        role="link"
        tabindex="0"
        :aria-label="`${item.title}，参考价 ${item.price} 元`"
        @click="goDetail(item)"
        @keydown.enter="goDetail(item)"
      >
        <div class="cover">
          <img v-if="getCover(item)" :src="getCover(item)" :alt="item.title" loading="lazy" />
          <i v-else class="cover-letter">{{ (item.title || '·').slice(0, 1) }}</i>
          <span v-if="item.qualityGrade" class="grade">{{ item.qualityGrade }}</span>
        </div>
        <p class="title">{{ item.title }}</p>
        <p class="meta">
          <span class="price"><b>¥</b>{{ formatPrice(item.price) }}</span>
          <span class="cat">{{ item.categoryName || '' }}</span>
        </p>
        <p class="views">{{ item.viewCount ?? 0 }} 人看过</p>
      </article>
    </div>

    <p v-if="!loading && productList.length === 0" class="empty">
      没有符合条件的商品。<br />
      换个关键词，或把价格区间放宽一点试试。
    </p>

    <pagination
      v-show="total > 0"
      v-model:page="queryParams.pageNum"
      v-model:limit="queryParams.pageSize"
      :total="total"
      @pagination="getList"
    />
  </div>
</template>

<script setup name="Market" lang="ts">
import { Search } from '@element-plus/icons-vue';
import { getCurrentInstance, onMounted, reactive, ref, toRefs } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { listCategoryTree } from '@/api/trade/category';
import { listOnSaleProduct } from '@/api/trade/product';
import { getDicts } from '@/api/system/dict/data';
import { ProductQuery, ProductVO, CategoryTreeVO } from '@/api/trade/types';

const { proxy } = getCurrentInstance() as any;
const { product_condition: conditionDict } = toRefs<any>(proxy?.useDict('product_condition'));

const router = useRouter();
const route = useRoute();
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

const formatPrice = (v?: number) => (v == null ? '—' : Number(v).toLocaleString('zh-CN'));

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
  // 支持从首页带参数进入:?keyword=xx / ?categoryId=xx
  const kw = route.query.keyword as string;
  const catId = route.query.categoryId as string;
  if (kw) {
    queryParams.title = kw;
  }
  if (catId) {
    queryParams.categoryId = Number(catId);
    categoryValue.value = [Number(catId)];
  }
  loadCategory();
  loadBrandDict();
  getList();
});
</script>

<style lang="scss" scoped>
/* ---------- 筛选条 ---------- */
.filter {
  margin-bottom: 16px;
  padding: 14px 16px;
  border: 1px solid var(--sm-card-border);
  border-radius: 16px;
  background: var(--sm-paper);
}

.filter-row {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;

  :deep(.el-input__wrapper),
  :deep(.el-select__wrapper) {
    box-shadow: 0 0 0 1.5px var(--sm-line) inset;
  }
}

.f-search {
  flex: 1 1 240px;
  min-width: 200px;
  max-width: 340px;
}

.f-cat,
.f-brand {
  flex: 0 0 auto;
  width: 168px;
}

.f-cond {
  flex: 0 0 auto;
  width: 142px;
}

/* 价格区间：两个输入合成一个控件 */
.f-price {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 0 10px;
  height: 32px;
  border-radius: var(--sm-r-sm);
  box-shadow: 0 0 0 1.5px var(--sm-line) inset;
  background: var(--sm-paper);

  /* el-input-number 默认 150px，不压住会把整行挤到换行 */
  :deep(.el-input-number) {
    width: 76px;
  }

  :deep(.el-input__wrapper) {
    width: 100%;
    padding: 0;
    box-shadow: none !important;
    background: transparent;
  }

  :deep(.el-input__inner) {
    text-align: center;
    font-family: var(--sm-font-num);
    font-size: 13px;
  }

  .dash {
    color: var(--sm-line);
    font-weight: 700;
  }
}

.f-actions {
  display: flex;
  flex: 0 0 auto;
  gap: 8px;
  margin-left: auto;
}

/* ---------- 商品格 ---------- */
.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(206px, 1fr));
  gap: 24px 18px;
  min-height: 180px;
}

.good {
  min-width: 0;
  cursor: pointer;
  border-radius: 4px;

  &:hover {
    .cover img {
      transform: scale(1.045);
    }

    .title {
      color: var(--sm-brand-deep);
    }
  }

  &:focus-visible {
    outline: 2px solid var(--sm-brand);
    outline-offset: 4px;
  }
}

.cover {
  position: relative;
  aspect-ratio: 4 / 3;
  border-radius: 14px;
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
    transition: transform 0.5s var(--sm-ease-out);
  }
}

.cover-letter {
  font-style: normal;
  font-size: 26px;
  font-weight: 800;
  color: var(--sm-brand-deep);
  opacity: 0.45;
}

.grade {
  position: absolute;
  top: 9px;
  left: 9px;
  min-width: 22px;
  padding: 2px 7px;
  border-radius: 7px;
  background: rgba(255, 255, 255, 0.94);
  color: var(--sm-brand-deep);
  font-family: var(--sm-font-num);
  font-size: 11.5px;
  font-weight: 800;
  text-align: center;
  box-shadow: 0 1px 3px rgba(16, 45, 80, 0.12);
}

.title {
  margin: 11px 0 0;
  font-size: 13.5px;
  font-weight: 600;
  line-height: 1.5;
  color: var(--sm-ink);
  /* 固定两行高度，让同一行的价格基线对齐 */
  min-height: 40px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  transition: color 0.18s ease;
}

.meta {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 10px;
  margin: 7px 0 0;
}

.price {
  font-family: var(--sm-font-num);
  font-size: 19px;
  font-weight: 800;
  letter-spacing: -0.02em;
  color: var(--sm-ink);
  font-variant-numeric: tabular-nums;

  b {
    margin-right: 1px;
    font-size: 12.5px;
    font-weight: 700;
    color: var(--sm-stone);
  }
}

.cat {
  flex-shrink: 0;
  font-size: 11.5px;
  font-weight: 600;
  color: var(--sm-stone);
}

.views {
  margin: 2px 0 0;
  font-size: 11.5px;
  font-weight: 500;
  color: var(--sm-stone);
  opacity: 0.75;
}

.empty {
  margin: 8px 0 0;
  padding: 56px 0;
  text-align: center;
  font-size: 13.5px;
  font-weight: 600;
  line-height: 1.9;
  color: var(--sm-stone);
}

/* ---------- 响应式 ---------- */
@media (max-width: 900px) {
  .filter-row {
    gap: 8px;
  }

  .f-search {
    max-width: none;
    flex: 1 1 100%;
  }

  .f-cat,
  .f-brand,
  .f-cond {
    flex: 1 1 calc(50% - 4px);
    width: auto;
  }

  .f-price {
    flex: 1 1 100%;
    justify-content: center;
  }

  .f-actions {
    margin-left: 0;
    flex: 1 1 100%;

    :deep(.el-button) {
      flex: 1;
    }
  }

  .grid {
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
    gap: 18px 12px;
  }
}
</style>
