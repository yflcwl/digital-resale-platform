<template>
  <div class="app-container" v-loading="loading">
    <el-row :gutter="20" v-if="product">
      <!-- 左侧轮播 -->
      <el-col :span="10">
        <el-card shadow="never">
          <el-carousel v-if="product.images && product.images.length > 0" height="380px" trigger="click">
            <el-carousel-item v-for="img in product.images" :key="img.id ?? img.url">
              <el-image :src="img.url" fit="contain" style="width: 100%; height: 100%" />
            </el-carousel-item>
          </el-carousel>
          <el-empty v-else description="暂无图片" :image-size="80" />
        </el-card>
      </el-col>

      <!-- 右侧信息 -->
      <el-col :span="14">
        <el-card shadow="never">
          <h2 class="detail-title">{{ product.title }}</h2>
          <div class="price-box">
            <span class="price">¥{{ product.price }}</span>
            <span v-if="product.originalPrice" class="original-price">原价 ¥{{ product.originalPrice }}</span>
            <el-tag v-if="product.qualityGrade" type="warning" size="large" style="margin-left: 12px">{{ product.qualityGrade }}级成色</el-tag>
            <el-tag v-if="product.conditionSelf" type="info" size="large" style="margin-left: 8px">{{ conditionSelfLabel(product.conditionSelf) }}</el-tag>
          </div>

          <el-descriptions :column="2" border class="detail-desc">
            <el-descriptions-item label="分类">{{ product.categoryName }}</el-descriptions-item>
            <el-descriptions-item label="品牌">{{ product.brandName || '-' }}</el-descriptions-item>
            <el-descriptions-item label="发布时间">{{ product.createTime }}</el-descriptions-item>
            <el-descriptions-item label="浏览">{{ product.viewCount }}</el-descriptions-item>
          </el-descriptions>

          <div class="description-block">
            <h4>商品描述</h4>
            <p>{{ product.description || '卖家很懒,没有写描述' }}</p>
          </div>

          <div class="action-bar">
            <el-button type="primary" size="large" @click="handleFavorite">
              {{ product.favorite ? '已收藏' : '收藏' }}
            </el-button>
            <el-button type="danger" size="large" @click="goChat">联系卖家</el-button>
          </div>
        </el-card>

        <!-- 卖家信息 -->
        <el-card shadow="never" class="seller-card">
          <template #header><span>卖家信息</span></template>
          <div class="seller-info">
            <el-avatar :size="48" :src="product.sellerAvatar">{{ (product.sellerNickname || '卖').charAt(0) }}</el-avatar>
            <div class="seller-meta">
              <div class="seller-name">{{ product.sellerNickname || '卖家' }}</div>
              <div class="seller-rating">综合评分:{{ product.sellerRating ?? 5.0 }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup name="ProductDetail" lang="ts">
import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { getProduct, favoriteProduct, cancelFavoriteProduct } from '@/api/trade/product';
import { conditionSelfOptions, ProductVO } from '@/api/trade/types';
import { ElMessage } from 'element-plus';

const route = useRoute();
const router = useRouter();
const loading = ref(false);
const product = ref<ProductVO>();

const load = async () => {
  loading.value = true;
  try {
    product.value = await getProduct(route.query.id as string);
  } finally {
    loading.value = false;
  }
};

const conditionSelfLabel = (val?: string) => {
  return conditionSelfOptions.find((c) => c.value === val)?.label || val || '-';
};

const handleFavorite = async () => {
  if (!product.value) return;
  try {
    if (product.value.favorite) {
      await cancelFavoriteProduct(product.value.id);
      ElMessage.success('已取消收藏');
    } else {
      await favoriteProduct(product.value.id);
      ElMessage.success('收藏成功');
    }
    product.value.favorite = !product.value.favorite;
  } catch {
    // 未登录等异常已由拦截器提示
  }
};

const goChat = () => {
  router.push('/message');
};

onMounted(() => {
  load();
});
</script>

<style lang="scss" scoped>
.detail-title {
  margin: 0 0 12px;
  font-size: 22px;
}
.price-box {
  background: #fdf3f3;
  padding: 14px;
  border-radius: 4px;
  margin-bottom: 16px;
  .price {
    color: #f56c6c;
    font-size: 28px;
    font-weight: 700;
  }
  .original-price {
    margin-left: 10px;
    color: #909399;
    text-decoration: line-through;
    font-size: 14px;
  }
}
.detail-desc {
  margin-bottom: 16px;
}
.description-block {
  margin-bottom: 20px;
  h4 {
    margin: 0 0 8px;
  }
  p {
    color: #606266;
    line-height: 1.8;
  }
}
.action-bar {
  margin-bottom: 16px;
}
.seller-card {
  .seller-info {
    display: flex;
    align-items: center;
    .seller-meta {
      margin-left: 14px;
      .seller-name {
        font-size: 16px;
        font-weight: 600;
      }
      .seller-rating {
        margin-top: 4px;
        color: #e6a23c;
        font-size: 13px;
      }
    }
  }
}
</style>
