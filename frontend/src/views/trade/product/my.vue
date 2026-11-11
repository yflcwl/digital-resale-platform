<template>
  <div class="app-container">
    <header class="sm-page-head">
      <div>
        <h1 class="sm-page-title">我的商品</h1>
        <p class="sm-page-desc">草稿需要先通过 AI 质检，才能上架到商品市场。</p>
      </div>
      <el-button type="primary" @click="goPublish">发布商品</el-button>
    </header>

    <div class="filter">
      <el-input v-model="queryParams.title" class="f-search" placeholder="标题关键词" clearable @keyup.enter="handleQuery">
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>

      <el-select v-model="queryParams.status" placeholder="全部状态" clearable class="f-status" @change="handleQuery">
        <el-option v-for="s in productStatusOptions" :key="s.value" :label="s.label" :value="s.value" />
      </el-select>

      <el-button type="primary" @click="handleQuery">查询</el-button>
    </div>

    <el-table v-loading="loading" :data="productList" class="table">
      <el-table-column label="商品" min-width="300">
        <template #default="scope">
          <div class="cell-product">
            <div class="cell-cover">
              <img v-if="getCover(scope.row)" :src="getCover(scope.row)" alt="" loading="lazy" />
              <i v-else>{{ (scope.row.title || '·').slice(0, 1) }}</i>
            </div>
            <div class="cell-main">
              <p class="cell-title">{{ scope.row.title }}</p>
              <p class="cell-sub">{{ scope.row.categoryName || '未分类' }} · {{ scope.row.createTime }}</p>
            </div>
          </div>
        </template>
      </el-table-column>

      <el-table-column label="价格" width="120">
        <template #default="scope">
          <span class="cell-price"><b>¥</b>{{ formatPrice(scope.row.price) }}</span>
        </template>
      </el-table-column>

      <el-table-column label="成色" width="90">
        <template #default="scope">
          <span v-if="scope.row.qualityGrade" class="sm-grade">{{ scope.row.qualityGrade }}</span>
          <span v-else class="cell-dash">—</span>
        </template>
      </el-table-column>

      <el-table-column label="状态" width="110">
        <template #default="scope">
          <span :class="['cell-status', `is-${(scope.row.status || '').toLowerCase()}`]">
            {{ productStatusDict[scope.row.status] || scope.row.status }}
          </span>
        </template>
      </el-table-column>

      <el-table-column label="操作" width="250" align="right">
        <template #default="scope">
          <div class="cell-actions">
            <button type="button" class="act" @click="goDetail(scope.row)">查看</button>
            <button v-if="canEdit(scope.row)" type="button" class="act" @click="goEdit(scope.row)">编辑</button>
            <button v-if="canEdit(scope.row)" type="button" class="act is-brand" @click="goInspection(scope.row)">去质检</button>
            <button v-if="scope.row.status === 'ON_SALE'" type="button" class="act" @click="handleOffShelf(scope.row)">下架</button>
            <button v-if="scope.row.status === 'OFF_SHELF'" type="button" class="act is-brand" @click="handleOnShelf(scope.row)">上架</button>
            <button v-if="canEdit(scope.row)" type="button" class="act is-danger" @click="handleDelete(scope.row)">删除</button>
          </div>
        </template>
      </el-table-column>

      <template #empty>
        <p class="table-empty">还没有发布过商品。点右上角「发布商品」，拍几张照片就能开始质检。</p>
      </template>
    </el-table>

    <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
  </div>
</template>

<script setup name="MyProduct" lang="ts">
import { Search } from '@element-plus/icons-vue';
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

const formatPrice = (v?: number) => (v == null ? '—' : Number(v).toLocaleString('zh-CN'));

const canEdit = (row: ProductVO) => ['DRAFT', 'OFF_SHELF'].includes(row.status as string);

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

const goPublish = () => {
  router.push('/trade/publish');
};

const goDetail = (row: ProductVO) => {
  router.push('/trade/detail?id=' + row.id);
};

const goInspection = (row: ProductVO) => {
  router.push('/trade/inspectionTask?productId=' + row.id);
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
/* ---------- 筛选条 ---------- */
.filter {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
  margin-bottom: 16px;
  padding: 14px 16px;
  border: 1px solid var(--sm-card-border);
  border-radius: 16px;
  background: var(--sm-paper);
}

.f-search {
  flex: 0 1 280px;
  min-width: 180px;
}

.f-status {
  width: 150px;
}

/* ---------- 表格单元 ---------- */
.table {
  border: 1px solid var(--sm-card-border);
  border-radius: 16px;
}

.cell-product {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 4px 0;
}

.cell-cover {
  flex: 0 0 48px;
  width: 48px;
  height: 48px;
  border-radius: 10px;
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

  i {
    font-style: normal;
    font-size: 16px;
    font-weight: 800;
    color: var(--sm-brand-deep);
    opacity: 0.4;
  }
}

.cell-main {
  min-width: 0;
}

.cell-title {
  margin: 0;
  font-size: 13.5px;
  font-weight: 700;
  color: var(--sm-ink);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.cell-sub {
  margin: 3px 0 0;
  font-size: 11.5px;
  font-weight: 500;
  color: var(--sm-stone);
}

.cell-price {
  font-family: var(--sm-font-num);
  font-size: 15px;
  font-weight: 800;
  color: var(--sm-ink);
  font-variant-numeric: tabular-nums;

  b {
    margin-right: 1px;
    font-size: 11px;
    font-weight: 700;
    color: var(--sm-stone);
  }
}

.cell-dash {
  color: var(--sm-line);
}

/* 状态：语义色区分，不用一水儿灰标签 */
.cell-status {
  display: inline-block;
  padding: 3px 10px;
  border-radius: var(--sm-r-pill);
  font-size: 11.5px;
  font-weight: 700;
  background: var(--sm-bg-2);
  color: var(--sm-ink-3);

  &.is-on_sale {
    background: var(--sm-accent-soft);
    color: #1c7a52;
  }

  &.is-waiting_qc {
    background: var(--sm-brand-soft);
    color: var(--sm-brand-deep);
  }

  &.is-locked {
    background: #fdf0dc;
    color: #9a6300;
  }

  &.is-sold {
    background: #fdeceb;
    color: #a83228;
  }
}

/* ---------- 行内操作 ---------- */
.cell-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  flex-wrap: wrap;
  gap: 10px;
}

.act {
  padding: 0;
  border: none;
  background: none;
  font-family: inherit;
  font-size: 12.5px;
  font-weight: 600;
  color: var(--sm-ink-3);
  cursor: pointer;
  transition: color 0.16s ease;

  &:hover {
    color: var(--sm-ink);
  }

  &.is-brand {
    color: var(--sm-brand-deep);

    &:hover {
      color: var(--sm-brand);
    }
  }

  &.is-danger:hover {
    color: var(--sm-danger);
  }
}

.table-empty {
  margin: 0;
  padding: 40px 0;
  font-size: 13px;
  font-weight: 600;
  color: var(--sm-stone);
}
</style>
