<template>
  <div class="app-container">
    <header class="sm-page-head">
      <div>
        <h1 class="sm-page-title">我的收藏</h1>
        <p class="sm-page-desc">收藏的二手好物都在这里，下架了也能看到。</p>
      </div>
    </header>

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
          <span :class="['state', { off: item.status !== 'ON_SALE' }]">
            {{ item.status === 'ON_SALE' ? '在售' : productStatusDict[item.status] || item.status }}
          </span>
        </p>

        <button type="button" class="unfav" @click.stop="handleCancel(item)">取消收藏</button>
      </article>
    </div>

    <p v-if="!loading && productList.length === 0" class="empty">
      还没有收藏。<br />
      在商品市场看到中意的，点「收藏」就会出现在这里。
    </p>

    <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
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

const formatPrice = (v?: number) => (v == null ? '—' : Number(v).toLocaleString('zh-CN'));

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
.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(206px, 1fr));
  gap: 24px 18px;
  min-height: 160px;
}

.good {
  position: relative;
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

    .unfav {
      opacity: 1;
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

.state {
  flex-shrink: 0;
  padding: 2px 8px;
  border-radius: var(--sm-r-pill);
  background: var(--sm-accent-soft);
  color: #1c7a52;
  font-size: 11px;
  font-weight: 700;

  &.off {
    background: var(--sm-bg-2);
    color: var(--sm-ink-3);
  }
}

.unfav {
  margin-top: 8px;
  padding: 0;
  border: none;
  background: none;
  font-family: inherit;
  font-size: 11.5px;
  font-weight: 600;
  color: var(--sm-stone);
  cursor: pointer;
  opacity: 0;
  transition:
    opacity 0.18s ease,
    color 0.16s ease;

  &:hover {
    color: var(--sm-danger);
  }

  &:focus-visible {
    opacity: 1;
    outline: 2px solid var(--sm-brand);
    outline-offset: 2px;
  }
}

.empty {
  margin: 0;
  padding: 72px 0;
  text-align: center;
  font-size: 13.5px;
  font-weight: 600;
  line-height: 1.9;
  color: var(--sm-stone);
}

@media (max-width: 900px) {
  .grid {
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
    gap: 18px 12px;
  }

  .unfav {
    opacity: 1;
  }
}
</style>
