<template>
  <div class="app-container seller-page" v-loading="loading">
    <!-- 卖家信息卡 -->
    <el-card shadow="never" class="seller-head" v-if="home">
      <div class="head-content">
        <el-avatar :size="72" :src="home.avatar">{{ (home.nickname || '卖').charAt(0) }}</el-avatar>
        <div class="head-info">
          <h2>{{ home.nickname }}</h2>
          <p class="bio">{{ home.bio || '卖家还没有填写简介' }}</p>
          <div class="stats">
            <span
              >综合评分 <b>{{ home.sellerRating ?? 5.0 }}</b></span
            >
            <span
              >收到评价 <b>{{ home.ratingCount ?? 0 }}</b></span
            >
            <span
              >成交 <b>{{ home.dealCount ?? 0 }}</b> 笔</span
            >
            <span
              >在售 <b>{{ home.onSaleCount ?? 0 }}</b> 件</span
            >
          </div>
        </div>
      </div>
    </el-card>

    <!-- Tabs -->
    <el-card shadow="never">
      <el-tabs v-model="tab" @tab-change="handleTabChange">
        <el-tab-pane label="在售商品" name="products">
          <el-empty v-if="!loading && products.length === 0" description="暂无在售商品" />
          <el-row :gutter="16">
            <el-col :xs="12" :sm="8" :md="6" :lg="4" v-for="item in products" :key="item.id" class="product-col">
              <el-card shadow="hover" class="product-card" @click="goProduct(item)">
                <div class="product-cover">
                  <el-image :src="cover(item)" fit="cover" class="cover-img">
                    <template #error
                      ><div class="image-slot">
                        <el-icon><picture /></el-icon></div
                    ></template>
                  </el-image>
                  <el-tag v-if="item.qualityGrade" size="small" class="grade-tag" type="warning">{{ item.qualityGrade }}级</el-tag>
                </div>
                <div class="product-title">{{ item.title }}</div>
                <div class="product-price">¥{{ item.price }}</div>
              </el-card>
            </el-col>
          </el-row>
          <pagination
            v-show="productTotal > 0"
            v-model:page="productPage.pageNum"
            v-model:limit="productPage.pageSize"
            :total="productTotal"
            @pagination="loadProducts"
          />
        </el-tab-pane>

        <el-tab-pane :label="`评价(${reviewTotal})`" name="reviews">
          <el-empty v-if="!loading && reviews.length === 0" description="暂无评价" />
          <div v-for="r in reviews" :key="r.id" class="review-item">
            <el-avatar :size="36" :src="r.reviewerAvatar">{{ (r.reviewerNickname || '买').charAt(0) }}</el-avatar>
            <div class="review-main">
              <div class="review-head">
                <span class="reviewer">{{ r.reviewerNickname || '买家' }}</span>
                <el-rate :model-value="r.rating" disabled size="small" />
                <span class="review-time">{{ r.createTime }}</span>
              </div>
              <div class="review-content">{{ r.content || '买家未填写评价内容' }}</div>
            </div>
          </div>
          <pagination
            v-show="reviewTotal > 0"
            v-model:page="reviewPage.pageNum"
            v-model:limit="reviewPage.pageSize"
            :total="reviewTotal"
            @pagination="loadReviews"
          />
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup name="SellerHome" lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { getSellerHome, pageSellerProducts, pageSellerReviews, SellerHomeVO, SellerReviewVO } from '@/api/trade/seller';
import { ProductVO } from '@/api/trade/types';

const route = useRoute();
const router = useRouter();
const loading = ref(false);
const tab = ref('products');
const home = ref<SellerHomeVO>();
const sellerId = ref<number>();

const products = ref<ProductVO[]>([]);
const productTotal = ref(0);
const productPage = reactive({ pageNum: 1, pageSize: 12 });

const reviews = ref<SellerReviewVO[]>([]);
const reviewTotal = ref(0);
const reviewPage = reactive({ pageNum: 1, pageSize: 10 });

const cover = (item: ProductVO) => (item.images && item.images.length > 0 ? item.images[0].url : '');

const goProduct = (item: ProductVO) => {
  router.push('/trade/detail?id=' + item.id);
};

const loadProducts = async () => {
  if (!sellerId.value) return;
  loading.value = true;
  try {
    const res = await pageSellerProducts(sellerId.value, productPage.pageNum, productPage.pageSize);
    products.value = res.rows;
    productTotal.value = res.total;
  } finally {
    loading.value = false;
  }
};

const loadReviews = async () => {
  if (!sellerId.value) return;
  loading.value = true;
  try {
    const res = await pageSellerReviews(sellerId.value, reviewPage.pageNum, reviewPage.pageSize);
    reviews.value = res.rows;
    reviewTotal.value = res.total;
  } finally {
    loading.value = false;
  }
};

const handleTabChange = () => {
  if (tab.value === 'reviews' && reviews.value.length === 0) {
    loadReviews();
  }
};

onMounted(async () => {
  sellerId.value = Number(route.query.sellerId);
  if (!sellerId.value) return;
  loading.value = true;
  try {
    const res = await getSellerHome(sellerId.value);
    home.value = res.data;
    await loadProducts();
    loadReviews();
  } finally {
    loading.value = false;
  }
});
</script>

<style lang="scss" scoped>
.seller-head {
  margin-bottom: 14px;
  .head-content {
    display: flex;
    gap: 18px;
    align-items: center;
  }
  .head-info {
    flex: 1;
    h2 {
      margin: 0;
      font-size: 22px;
    }
    .bio {
      margin: 8px 0;
      color: var(--sm-ink-3);
      font-size: 13px;
    }
    .stats {
      display: flex;
      gap: 22px;
      color: var(--sm-ink-2);
      font-size: 13px;
      b {
        color: var(--sm-brand);
        font-size: 15px;
      }
    }
  }
}
.product-col {
  margin-bottom: 16px;
}
.product-card {
  cursor: pointer;
  .product-cover {
    position: relative;
    height: 150px;
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
      font-size: 26px;
    }
    .grade-tag {
      position: absolute;
      top: 6px;
      right: 6px;
    }
  }
  .product-title {
    margin-top: 8px;
    font-size: 14px;
    height: 40px;
    overflow: hidden;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
  }
  .product-price {
    margin-top: 6px;
    color: var(--sm-danger);
    font-size: 18px;
    font-weight: 600;
  }
}
.review-item {
  display: flex;
  gap: 12px;
  padding: 14px 0;
  border-bottom: 1px solid var(--el-border-color-lighter);
  .review-main {
    flex: 1;
    .review-head {
      display: flex;
      align-items: center;
      gap: 12px;
      .reviewer {
        font-weight: 500;
      }
      .review-time {
        margin-left: auto;
        color: var(--sm-ink-3);
        font-size: 12px;
      }
    }
    .review-content {
      margin-top: 6px;
      color: var(--sm-ink-2);
      font-size: 13px;
    }
  }
}
</style>
