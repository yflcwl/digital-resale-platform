<template>
  <div class="home">
    <!-- ============ Hero：全页唯一用力处 ============ -->
    <section class="hero" :class="{ 'is-single': !stack.length }">
      <div class="hero-copy">
        <p class="eyebrow">
          <i class="dot"></i>
          <span>在售 {{ totalText }} 件 · 每件都附 AI 质检报告</span>
        </p>

        <h1 class="hero-title">好成色<br /><em>看得见</em>。</h1>

        <p class="hero-sub">AI 标注每处缺陷，定五级成色，按行情给参考价。<br />谈价之前，先看报告。</p>

        <form class="hero-search" @submit.prevent="handleSearch">
          <el-icon class="s-icon"><Search /></el-icon>
          <input v-model="keyword" type="search" aria-label="搜索在售商品" placeholder="搜索 iPhone、MacBook、相机、耳机…" />
          <button type="submit" class="s-btn">搜索</button>
        </form>

        <div class="hero-hot">
          <span class="hot-label">常搜</span>
          <button v-for="w in hotWords" :key="w" type="button" class="hot-word" @click="searchWord(w)">{{ w }}</button>
        </div>
      </div>

      <!-- 真实在售商品，扇形摊开：图片本身就是最好的装饰 -->
      <div v-if="stack.length" class="hero-stack">
        <article
          v-for="(p, i) in stack"
          :key="p.id"
          :class="['sc', `sc-${i + 1}`]"
          role="link"
          tabindex="0"
          :aria-label="`${p.title}，成色 ${p.qualityGrade || '未评级'}，参考价 ${formatPrice(p.price)} 元`"
          @click="goDetail(p)"
          @keydown.enter="goDetail(p)"
        >
          <div class="sc-cover">
            <img v-if="p.images && p.images.length" :src="p.images[0].url" :alt="p.title" />
            <i v-else class="sc-letter">{{ (p.title || '·').slice(0, 1) }}</i>
          </div>
          <div class="sc-foot">
            <span v-if="p.qualityGrade" class="sc-grade">{{ p.qualityGrade }}</span>
            <span class="sc-price">¥{{ formatPrice(p.price) }}</span>
          </div>
        </article>
      </div>
    </section>

    <!-- ============ 活动横幅：仅后台配了轮播才出现 ============ -->
    <section v-if="carousels.length" class="promo">
      <el-carousel height="116px" :interval="6000" arrow="never" indicator-position="none">
        <el-carousel-item v-for="c in carousels" :key="c.id" @click="goBanner(c)">
          <div class="promo-slide">
            <img :src="c.imageUrl" :alt="c.title" />
            <span class="promo-title">{{ c.title }}</span>
          </div>
        </el-carousel-item>
      </el-carousel>
    </section>

    <!-- ============ 分类：纯文字条 ============ -->
    <nav v-if="categories.length" class="cats">
      <button v-for="c in categories" :key="c.id" type="button" class="cat" @click="goCategory(c.id)">
        {{ c.name }}
      </button>
    </nav>

    <!-- ============ 信任条：一行说完凭什么可信 ============ -->
    <p class="trust">
      <span>AI 逐项标注缺陷</span>
      <i></i>
      <span>S / A / B / C / D 五级成色</span>
      <i></i>
      <span>参考价按行情折损计算</span>
    </p>

    <!-- ============ 精选好物 ============ -->
    <section class="goods">
      <header class="sec-head">
        <h2>精选好物</h2>
        <button type="button" class="more" @click="goMarket()">
          看全部在售
          <svg viewBox="0 0 16 16" aria-hidden="true">
            <path d="M3 8h9M8.5 4.5 12 8l-3.5 3.5" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" />
          </svg>
        </button>
      </header>

      <div v-loading="loading" class="grid">
        <article
          v-for="p in goods"
          :key="p.id"
          class="good"
          role="link"
          tabindex="0"
          :aria-label="`${p.title}，参考价 ${formatPrice(p.price)} 元`"
          @click="goDetail(p)"
          @keydown.enter="goDetail(p)"
        >
          <div class="good-cover">
            <img v-if="p.images && p.images.length" :src="p.images[0].url" :alt="p.title" loading="lazy" />
            <i v-else class="thumb-letter">{{ (p.title || '·').slice(0, 1) }}</i>
            <span v-if="p.qualityGrade" class="good-grade">{{ p.qualityGrade }}</span>
          </div>
          <p class="good-title">{{ p.title }}</p>
          <p class="good-meta">
            <span class="good-price"><b>¥</b>{{ formatPrice(p.price) }}</span>
            <span class="good-cat">{{ p.categoryName || '' }}</span>
          </p>
        </article>

        <p v-if="!loading && goods.length === 0" class="empty">还没有在售商品。质检通过后会自动出现在这里。</p>
      </div>
    </section>
  </div>
</template>

<script setup name="Index" lang="ts">
import { Search } from '@element-plus/icons-vue';
import { listOnSaleProduct, listFeaturedProduct } from '@/api/trade/product';
import { listCategoryTree } from '@/api/trade/category';
import { listCarousel, CarouselVO } from '@/api/trade/carousel';
import type { CategoryTreeVO, ProductVO } from '@/api/trade/types';

const router = useRouter();

const keyword = ref('');
const goods = ref<ProductVO[]>([]);
const loading = ref(false);
const total = ref(0);
const carousels = ref<CarouselVO[]>([]);
const categories = ref<CategoryTreeVO[]>([]);

const hotWords = ['iPhone', 'MacBook', '相机', '耳机', '游戏机', '智能手表'];

/** Hero 右侧扇形卡：取前 3 件真实在售 */
const stack = computed(() => goods.value.slice(0, 3));
const totalText = computed(() => (total.value ? total.value.toLocaleString('zh-CN') : '—'));

const formatPrice = (v?: number) => (v == null ? '—' : Number(v).toLocaleString('zh-CN'));

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
  goMarket(kw ? { keyword: kw } : {});
};

const searchWord = (w: string) => {
  keyword.value = w;
  handleSearch();
};

onMounted(async () => {
  loading.value = true;
  const [featuredRes, onSaleRes, catsRes, carouselRes] = await Promise.allSettled([
    listFeaturedProduct(8),
    listOnSaleProduct({ pageNum: 1, pageSize: 8 } as any),
    listCategoryTree(),
    listCarousel()
  ]);

  const featured = featuredRes.status === 'fulfilled' ? featuredRes.value.data || [] : [];
  const onSaleRows = onSaleRes.status === 'fulfilled' ? onSaleRes.value.rows || [] : [];
  // 管理员尚未设置精选时，回落展示最新上架
  goods.value = featured.length ? featured : onSaleRows;
  total.value = onSaleRes.status === 'fulfilled' ? (onSaleRes.value as any).total ?? onSaleRows.length : onSaleRows.length;

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
  max-width: 1320px;
  margin: 0 auto;
  padding: 22px 28px 56px;
}

/* ==================== Hero ==================== */
.hero {
  position: relative;
  display: grid;
  grid-template-columns: minmax(0, 1fr) 440px;
  align-items: center;
  gap: 32px;
  padding: 52px 56px 48px;
  border-radius: 26px;
  background: var(--sm-ink);
  overflow: hidden;

  /* 两团极淡的光，让深色不闷 */
  &::before,
  &::after {
    content: '';
    position: absolute;
    border-radius: 50%;
    pointer-events: none;
  }

  &::before {
    width: 620px;
    height: 620px;
    top: -320px;
    left: -160px;
    background: radial-gradient(circle, rgba(43, 156, 240, 0.34), transparent 66%);
  }

  &::after {
    width: 460px;
    height: 460px;
    right: -180px;
    bottom: -240px;
    background: radial-gradient(circle, rgba(79, 201, 138, 0.2), transparent 68%);
  }

  &.is-single {
    grid-template-columns: minmax(0, 1fr);
    max-width: 720px;
  }
}

.hero-copy {
  position: relative;
  z-index: 2;
  min-width: 0;
}

.eyebrow {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  margin: 0;
  font-size: 12.5px;
  font-weight: 600;
  letter-spacing: 0.01em;
  color: rgba(214, 232, 250, 0.72);

  .dot {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    background: var(--sm-accent);
    box-shadow: 0 0 0 3px rgba(79, 201, 138, 0.18);
  }
}

.hero-title {
  margin: 18px 0 0;
  font-size: clamp(40px, 4.4vw, 60px);
  font-weight: 800;
  line-height: 1.04;
  letter-spacing: -0.035em;
  color: #fff;

  em {
    font-style: normal;
    color: #63b8f7;
  }
}

.hero-sub {
  margin: 16px 0 0;
  max-width: 26em;
  font-size: 14.5px;
  line-height: 1.7;
  font-weight: 500;
  color: rgba(200, 220, 240, 0.78);
  text-wrap: pretty;
}

/* 搜索：白输入 + 实体蓝键 */
.hero-search {
  position: relative;
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 26px;
  max-width: 560px;
  padding: 7px 7px 7px 16px;
  border-radius: var(--sm-r-pill);
  background: #fff;

  .s-icon {
    flex-shrink: 0;
    font-size: 17px;
    color: var(--sm-stone);
  }

  input {
    flex: 1;
    min-width: 0;
    height: 34px;
    border: none;
    outline: none;
    background: none;
    font-family: inherit;
    font-size: 14px;
    font-weight: 500;
    color: var(--sm-ink);

    &::placeholder {
      color: #9db0c4;
    }

    /* 去掉 type=search 的原生清除按钮，与整体圆角不搭 */
    &::-webkit-search-decoration,
    &::-webkit-search-cancel-button {
      -webkit-appearance: none;
    }
  }

  .s-btn {
    flex-shrink: 0;
    height: 40px;
    padding: 0 26px;
    border: none;
    border-radius: var(--sm-r-pill);
    background: var(--sm-brand);
    color: #fff;
    font-family: inherit;
    font-size: 14px;
    font-weight: 700;
    letter-spacing: 0.02em;
    cursor: pointer;
    box-shadow: 0 3px 0 var(--sm-brand-deep);
    transition:
      transform 0.16s var(--sm-ease),
      box-shadow 0.16s var(--sm-ease),
      background-color 0.16s ease;

    &:hover {
      background: #3aa6f2;
    }

    &:active {
      transform: translateY(3px);
      box-shadow: 0 0 0 var(--sm-brand-deep);
    }
  }
}

.hero-hot {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 6px;
  margin-top: 16px;

  .hot-label {
    margin-right: 2px;
    font-size: 12px;
    font-weight: 600;
    color: rgba(200, 220, 240, 0.5);
  }

  .hot-word {
    padding: 4px 11px;
    border: 1px solid rgba(255, 255, 255, 0.16);
    border-radius: var(--sm-r-pill);
    background: transparent;
    font-family: inherit;
    font-size: 12px;
    font-weight: 600;
    color: rgba(214, 232, 250, 0.82);
    cursor: pointer;
    transition:
      background-color 0.16s ease,
      border-color 0.16s ease,
      color 0.16s ease;

    &:hover {
      background: rgba(255, 255, 255, 0.1);
      border-color: rgba(255, 255, 255, 0.3);
      color: #fff;
    }
  }
}

/* 扇形商品卡 */
.hero-stack {
  position: relative;
  z-index: 1;
  width: 430px;
  height: 268px;
  justify-self: end;
}

.sc {
  position: absolute;
  width: 170px;
  padding: 7px 7px 9px;
  border-radius: 15px;
  background: rgba(255, 255, 255, 0.09);
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow:
    0 20px 44px -20px rgba(0, 0, 0, 0.7),
    0 0 0 1px rgba(255, 255, 255, 0.04) inset;
  cursor: pointer;
  transition: transform 0.45s var(--sm-ease);

  &:hover {
    transform: rotate(0deg) translateY(-6px);
    z-index: 5;
  }
}

.sc-1 {
  left: 0;
  top: 92px;
  transform: rotate(-8deg);
  z-index: 1;
}

.sc-2 {
  left: 130px;
  top: 4px;
  transform: rotate(-2deg);
  z-index: 3;
}

.sc-3 {
  left: 260px;
  top: 86px;
  transform: rotate(7deg);
  z-index: 2;
}

.sc-cover {
  height: 118px;
  border-radius: 10px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.08);
  display: flex;
  align-items: center;
  justify-content: center;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
  }
}

.sc-letter {
  font-style: normal;
  font-size: 26px;
  font-weight: 800;
  color: rgba(255, 255, 255, 0.6);
}

.sc-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
  margin-top: 9px;
  padding: 0 2px;
}

.sc-grade {
  flex-shrink: 0;
  min-width: 20px;
  padding: 1px 6px;
  border-radius: 6px;
  background: var(--sm-accent);
  color: #06301f;
  font-family: var(--sm-font-num);
  font-size: 11px;
  font-weight: 800;
  text-align: center;
}

.sc-price {
  font-family: var(--sm-font-num);
  font-size: 13.5px;
  font-weight: 800;
  color: #fff;
  letter-spacing: -0.01em;
  font-variant-numeric: tabular-nums;
}

/* ==================== 活动横幅 ==================== */
.promo {
  margin-top: 18px;
  border-radius: 18px;
  overflow: hidden;

  :deep(.el-carousel__container) {
    border-radius: 18px;
  }
}

.promo-slide {
  position: relative;
  height: 100%;
  cursor: pointer;

  /* 后台配的图片内容不可控，加一层左侧压暗，让标题一定读得清 */
  &::after {
    content: '';
    position: absolute;
    inset: 0;
    background: linear-gradient(90deg, rgba(16, 27, 43, 0.78) 0%, rgba(16, 27, 43, 0.34) 42%, transparent 72%);
    pointer-events: none;
  }

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
    filter: saturate(0.92);
  }
}

.promo-title {
  position: absolute;
  z-index: 2;
  left: 26px;
  top: 50%;
  transform: translateY(-50%);
  max-width: 52%;
  font-size: 15px;
  font-weight: 800;
  letter-spacing: 0.01em;
  line-height: 1.4;
  color: #fff;
  text-shadow: 0 1px 10px rgba(0, 0, 0, 0.35);
}

/* ==================== 分类文字条 ==================== */
.cats {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 4px;
  margin-top: 8px;
  padding: 14px 2px;
  border-bottom: 1px solid var(--sm-line-soft);
}

.cat {
  padding: 7px 15px;
  border: none;
  border-radius: var(--sm-r-pill);
  background: transparent;
  font-family: inherit;
  font-size: 13.5px;
  font-weight: 700;
  color: var(--sm-ink-2);
  cursor: pointer;
  transition:
    background-color 0.16s ease,
    color 0.16s ease;

  &:hover {
    background: var(--sm-brand-soft);
    color: var(--sm-brand-deep);
  }

  &:active {
    transform: scale(0.97);
  }
}

/* ==================== 信任条 ==================== */
.trust {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 14px;
  margin: 16px 0 0;
  padding: 0 2px;
  font-size: 12.5px;
  font-weight: 600;
  color: var(--sm-stone);

  span {
    white-space: nowrap;
  }

  i {
    width: 3px;
    height: 3px;
    border-radius: 50%;
    background: var(--sm-line);
  }
}

/* ==================== 精选好物 ==================== */
.goods {
  margin-top: 30px;
}

.sec-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;

  h2 {
    margin: 0;
    font-size: 20px;
    font-weight: 800;
    letter-spacing: -0.02em;
    color: var(--sm-ink);
  }
}

.more {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  padding: 0;
  border: none;
  background: none;
  font-family: inherit;
  font-size: 13px;
  font-weight: 700;
  color: var(--sm-brand-deep);
  cursor: pointer;

  svg {
    width: 14px;
    height: 14px;
    transition: transform 0.2s var(--sm-ease);
  }

  &:hover {
    color: var(--sm-brand);

    svg {
      transform: translateX(3px);
    }
  }
}

.grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 22px 18px;
  min-height: 140px;
}

.good {
  min-width: 0;
  cursor: pointer;

  &:hover {
    .good-cover img {
      transform: scale(1.045);
    }

    .good-title {
      color: var(--sm-brand-deep);
    }
  }
}

.good-cover {
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

.thumb-letter {
  font-style: normal;
  font-size: 28px;
  font-weight: 800;
  color: var(--sm-brand-deep);
  opacity: 0.5;
}

.good-grade {
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

.good-title {
  margin: 11px 0 0;
  font-size: 13.5px;
  font-weight: 600;
  line-height: 1.5;
  color: var(--sm-ink);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  transition: color 0.18s ease;
}

.good-meta {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 10px;
  margin: 7px 0 0;
}

.good-price {
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

.good-cat {
  flex-shrink: 0;
  font-size: 11.5px;
  font-weight: 600;
  color: var(--sm-stone);
}

.empty {
  grid-column: 1 / -1;
  margin: 8px 0 0;
  padding: 36px 0;
  text-align: center;
  font-size: 13px;
  font-weight: 600;
  color: var(--sm-stone);
}

/* ==================== 无障碍：键盘焦点 ==================== */
.s-btn:focus-visible,
.hot-word:focus-visible,
.cat:focus-visible,
.more:focus-visible,
.sc:focus-visible,
.hero-search input:focus-visible {
  outline: 2px solid #63b8f7;
  outline-offset: 2px;
}

.hero-search input:focus-visible {
  outline-offset: 0;
  border-radius: 8px;
}

/* 只做视觉提示的元素（商品图、徽章）不参与 tab 序 */
.good:focus-visible {
  outline: 2px solid var(--sm-brand);
  outline-offset: 3px;
  border-radius: 4px;
}

/* ==================== 尊重减少动效偏好 ==================== */
@media (prefers-reduced-motion: reduce) {
  .sc,
  .sc:hover,
  .cat:active,
  .good-cover img,
  .more svg,
  .s-btn,
  .s-btn:active {
    transform: none !important;
    transition: none !important;
  }
}

/* ==================== 响应式 ==================== */
@media (max-width: 1240px) {
  .hero {
    grid-template-columns: minmax(0, 1fr) 380px;
    padding: 44px 40px 42px;
  }

  .hero-stack {
    width: 384px;
    height: 244px;
  }

  .sc {
    width: 154px;
  }

  .sc-1 {
    left: 0;
    top: 82px;
  }

  .sc-2 {
    left: 114px;
  }

  .sc-3 {
    left: 230px;
    top: 78px;
  }

  .sc-cover {
    height: 104px;
  }

  .grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 980px) {
  .hero {
    grid-template-columns: minmax(0, 1fr);
    padding: 38px 30px 36px;
  }

  .hero-stack {
    display: none;
  }

  .grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 640px) {
  .home {
    padding: 14px 14px 40px;
  }

  .hero {
    padding: 30px 20px 28px;
    border-radius: 20px;
  }

  .hero-title {
    font-size: 34px;
  }

  .hero-search {
    padding-left: 13px;

    .s-btn {
      height: 38px;
      padding: 0 18px;
    }
  }

  .cats {
    gap: 2px;
  }

  .cat {
    padding: 6px 11px;
    font-size: 13px;
  }

  .trust {
    gap: 10px;
    font-size: 12px;
  }

  .grid {
    gap: 18px 12px;
  }
}
</style>
