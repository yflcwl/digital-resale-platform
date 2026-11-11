<template>
  <div class="home">
    <!-- 搜索横幅 -->
    <section class="banner">
      <el-carousel v-if="carousels.length" height="300px" :interval="5000" arrow="hover" class="banner-carousel">
        <el-carousel-item v-for="c in carousels" :key="c.id" @click="goBanner(c)">
          <div class="banner-slide">
            <img :src="c.imageUrl" :alt="c.title" />
            <div class="slide-mask">
              <h2 class="slide-title">{{ c.title }}</h2>
              <p class="slide-sub">AI 智能质检 · 成色分级 · 好物放心购</p>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
      <div v-else class="banner-fallback">
        <h2 class="slide-title">二手数码 · 好成色看得见</h2>
        <p class="slide-sub">AI 智能质检为每件商品定成色、给参考价</p>
      </div>

      <div class="banner-search">
        <el-input
          v-model="keyword"
          size="large"
          placeholder="搜索 iPhone、MacBook、相机、耳机…"
          class="search-input"
          clearable
          @keyup.enter="handleSearch"
        >
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-button type="primary" size="large" @click="handleSearch">搜 索</el-button>
      </div>

      <div class="hot-words">
        <span class="hot-label">热门:</span>
        <span v-for="w in hotWords" :key="w" class="hot-word" @click="searchWord(w)">{{ w }}</span>
      </div>
    </section>

    <!-- 分类入口 -->
    <section class="cat-section">
      <div class="cat-item" v-for="c in categories" :key="c.id" @click="goCategory(c.id)">
        <span class="cat-icon">{{ catEmoji(c.name) }}</span>
        <span class="cat-name">{{ c.name }}</span>
      </div>
    </section>

    <!-- 卖点 -->
    <section class="features">
      <div class="feature">
        <span class="feature-icon">🤖</span>
        <div>
          <b>AI 智能质检</b>
          <em>智能体从照片识别缺陷与成色</em>
        </div>
      </div>
      <div class="feature">
        <span class="feature-icon">🏷️</span>
        <div>
          <b>成色分级定价</b>
          <em>规则引擎给出 S/A/B/C/D 与参考价</em>
        </div>
      </div>
      <div class="feature">
        <span class="feature-icon">🛡️</span>
        <div>
          <b>质检后方可上架</b>
          <em>每件在售商品都带质检报告</em>
        </div>
      </div>
    </section>

    <!-- 精选好物 -->
    <section class="goods-section">
      <header class="goods-head">
        <h2 class="goods-title">精选好物</h2>
        <button type="button" class="goods-more" @click="goMarket()">查看全部 →</button>
      </header>

      <div v-loading="loading" class="goods-grid">
        <div v-for="p in goods" :key="p.id" class="good" @click="goDetail(p)">
          <div class="good-cover">
            <img v-if="p.images && p.images.length" :src="p.images[0].url" :alt="p.title" />
            <i v-else class="thumb-letter">{{ (p.title || '·').slice(0, 1) }}</i>
            <span v-if="p.qualityGrade" class="good-grade">{{ p.qualityGrade }}级</span>
          </div>
          <div class="good-title">{{ p.title }}</div>
          <div class="good-meta">
            <span class="good-price">¥{{ formatPrice(p.price) }}</span>
            <span class="good-cat">{{ p.categoryName || '' }}</span>
          </div>
        </div>

        <el-empty v-if="!loading && goods.length === 0" description="暂无在售商品" />
      </div>
    </section>
  </div>
</template>

<script setup name="Index" lang="ts">
import { Search } from '@element-plus/icons-vue';
import { listOnSaleProduct } from '@/api/trade/product';
import { listCategoryTree } from '@/api/trade/category';
import { listCarousel, CarouselVO } from '@/api/trade/carousel';
import type { CategoryTreeVO, ProductVO } from '@/api/trade/types';

const router = useRouter();

const keyword = ref('');
const goods = ref<ProductVO[]>([]);
const loading = ref(false);
const carousels = ref<CarouselVO[]>([]);
const categories = ref<CategoryTreeVO[]>([]);

const hotWords = ['iPhone', 'MacBook', '相机', '耳机', '游戏机', '智能手表'];

const formatPrice = (v?: number) => (v == null ? '—' : Number(v).toLocaleString('zh-CN'));

const catEmoji = (name: string) => {
  const map: Record<string, string> = {
    手机: '📱',
    平板: '📟',
    笔记本: '💻',
    相机: '📷',
    耳机: '🎧',
    游戏机: '🎮',
    智能穿戴: '⌚',
    配件: '🔌'
  };
  return map[name] || '📦';
};

const goDetail = (p: ProductVO) => router.push('/trade/detail?id=' + p.id);

const goMarket = (params?: Record<string, any>) => router.push({ path: '/trade/market', query: params || {} });

const goCategory = (id: number) => goMarket({ categoryId: id });

const goBanner = (c: CarouselVO) => {
  if (c.link) {
    router.push(c.link);
  } else {
    goMarket();
  }
};

const handleSearch = () => {
  const kw = keyword.value.trim();
  if (kw) {
    goMarket({ keyword: kw });
  } else {
    goMarket();
  }
};

const searchWord = (w: string) => {
  keyword.value = w;
  handleSearch();
};

onMounted(async () => {
  loading.value = true;
  const [goodsRes, catsRes, carouselRes] = await Promise.allSettled([
    listOnSaleProduct({ pageNum: 1, pageSize: 8 } as any),
    listCategoryTree(),
    listCarousel()
  ]);
  if (goodsRes.status === 'fulfilled') {
    goods.value = goodsRes.value.rows || [];
  }
  if (catsRes.status === 'fulfilled') {
    categories.value = (catsRes.value.data || []).slice(0, 8);
  }
  if (carouselRes.status === 'fulfilled') {
    carousels.value = carouselRes.value.data || [];
  }
  loading.value = false;
});
</script>

<style lang="scss" scoped>
.home {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px 28px 44px;
}

/* ---------- 搜索横幅 ---------- */
.banner {
  position: relative;
  border-radius: 20px;
  overflow: hidden;
  border: 1px solid var(--sm-card-border);
  background: var(--sm-paper);
  box-shadow: 0 1px 2px rgba(16, 45, 80, 0.04);
}

.banner-carousel {
  :deep(.el-carousel__indicators--horizontal) {
    left: 50%;
    transform: translateX(-50%);
  }
}

.banner-slide {
  position: relative;
  width: 100%;
  height: 100%;
  cursor: pointer;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.slide-mask {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 0 48px;
  background: linear-gradient(90deg, rgba(8, 24, 42, 0.62) 0%, rgba(8, 24, 42, 0.18) 55%, transparent 100%);
}

.slide-title {
  margin: 0;
  color: #fff;
  font-size: 30px;
  font-weight: 800;
  letter-spacing: 0.02em;
  text-shadow: 0 2px 12px rgba(0, 0, 0, 0.25);
}

.slide-sub {
  margin: 10px 0 0;
  color: rgba(255, 255, 255, 0.88);
  font-size: 14px;
  font-weight: 500;
}

.banner-fallback {
  position: relative;
  height: 220px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  padding: 0 48px;
  background:
    radial-gradient(circle at 78% 20%, rgba(79, 201, 138, 0.35), transparent 45%),
    linear-gradient(120deg, var(--sm-brand-deep), var(--sm-brand));

  .slide-title,
  .slide-sub {
    color: #fff;
  }
}

.banner-search {
  position: relative;
  z-index: 3;
  display: flex;
  gap: 10px;
  max-width: 640px;
  margin: -26px auto 0;
  padding: 0 20px;

  :deep(.el-input__wrapper) {
    border-radius: var(--sm-r-pill);
    padding-left: 18px;
    box-shadow: 0 8px 24px rgba(16, 45, 80, 0.14);
  }

  :deep(.el-button) {
    box-shadow: 0 8px 24px rgba(43, 156, 240, 0.28);
  }
}

.hot-words {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-wrap: wrap;
  gap: 8px;
  padding: 14px 20px 18px;
}

.hot-label {
  font-size: 12px;
  font-weight: 700;
  color: var(--sm-stone);
}

.hot-word {
  font-size: 12px;
  font-weight: 600;
  color: var(--sm-ink-3);
  background: var(--sm-bg);
  border: 1px solid var(--sm-line-soft);
  border-radius: var(--sm-r-pill);
  padding: 3px 12px;
  cursor: pointer;
  transition: all var(--sm-dur) ease;

  &:hover {
    color: var(--sm-brand-deep);
    background: var(--sm-brand-soft);
    border-color: var(--sm-brand-soft);
  }
}

/* ---------- 分类入口 ---------- */
.cat-section {
  margin-top: 18px;
  display: grid;
  grid-template-columns: repeat(8, minmax(0, 1fr));
  gap: 12px;
  padding: 16px 18px;
  border-radius: 18px;
  border: 1px solid var(--sm-card-border);
  background: var(--sm-paper);
  box-shadow: 0 1px 2px rgba(16, 45, 80, 0.04);
}

.cat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 10px 4px;
  border-radius: var(--sm-r-sm);
  cursor: pointer;
  transition:
    background-color var(--sm-dur) ease,
    transform var(--sm-dur) var(--sm-ease);

  &:hover {
    background: var(--sm-brand-tint);
    transform: translateY(-2px);
  }
}

.cat-icon {
  font-size: 26px;
  line-height: 1;
}

.cat-name {
  font-size: 12.5px;
  font-weight: 700;
  color: var(--sm-ink-2);
}

/* ---------- 卖点 ---------- */
.features {
  margin-top: 16px;
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.feature {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 18px;
  border-radius: 16px;
  border: 1px solid var(--sm-card-border);
  background: var(--sm-paper);
  box-shadow: 0 1px 2px rgba(16, 45, 80, 0.04);

  .feature-icon {
    font-size: 24px;
  }

  b {
    display: block;
    font-size: 13.5px;
    font-weight: 800;
    color: var(--sm-ink);
  }

  em {
    font-style: normal;
    font-size: 12px;
    font-weight: 500;
    color: var(--sm-stone);
  }
}

/* ---------- 精选好物 ---------- */
.goods-section {
  margin-top: 20px;
  padding: 20px 22px 18px;
  border-radius: 18px;
  border: 1px solid var(--sm-card-border);
  background: var(--sm-paper);
  box-shadow: 0 1px 2px rgba(16, 45, 80, 0.04);
}

.goods-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin-bottom: 16px;
}

.goods-title {
  margin: 0;
  font-size: 18px;
  font-weight: 800;
  letter-spacing: -0.01em;
  color: var(--sm-ink);
}

.goods-more {
  border: none;
  background: none;
  padding: 0;
  font-size: 13px;
  font-weight: 700;
  color: var(--sm-brand);
  cursor: pointer;

  &:hover {
    opacity: 0.7;
  }
}

.goods-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
  min-height: 120px;
}

.good {
  cursor: pointer;
  transition: transform var(--sm-dur) var(--sm-ease);

  &:hover {
    transform: translateY(-3px);

    .good-cover img {
      transform: scale(1.05);
    }

    .good-title {
      color: var(--sm-brand-deep);
    }
  }
}

.good-cover {
  position: relative;
  height: 160px;
  border-radius: var(--sm-r-sm);
  overflow: hidden;
  background: var(--sm-brand-soft);
  display: flex;
  align-items: center;
  justify-content: center;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform var(--sm-dur-lg) var(--sm-ease-out);
  }
}

.thumb-letter {
  font-style: normal;
  font-size: 30px;
  font-weight: 800;
  color: var(--sm-brand-deep);
}

.good-grade {
  position: absolute;
  top: 8px;
  right: 8px;
  font-family: var(--sm-font-num);
  font-size: 11px;
  font-weight: 800;
  color: var(--sm-brand-deep);
  background: rgba(255, 255, 255, 0.92);
  border-radius: var(--sm-r-pill);
  padding: 2px 9px;
}

.good-title {
  margin-top: 10px;
  font-size: 13.5px;
  font-weight: 700;
  color: var(--sm-ink);
  line-height: 1.45;
  height: 39px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  transition: color var(--sm-dur) ease;
}

.good-meta {
  margin-top: 8px;
  display: flex;
  align-items: baseline;
  justify-content: space-between;
}

.good-price {
  font-family: var(--sm-font-num);
  font-size: 18px;
  font-weight: 800;
  color: var(--sm-danger);
}

.good-cat {
  font-size: 11.5px;
  font-weight: 600;
  color: var(--sm-stone);
}

/* ---------- 响应式 ---------- */
@media (max-width: 1180px) {
  .cat-section {
    grid-template-columns: repeat(4, minmax(0, 1fr));
  }

  .goods-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 860px) {
  .home {
    padding: 14px 14px 36px;
  }

  .banner-search {
    margin-top: -18px;
  }

  .slide-title {
    font-size: 22px;
  }

  .features {
    grid-template-columns: minmax(0, 1fr);
  }

  .goods-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
