<template>
  <div class="dash">
    <!-- 问候 + 搜索 -->
    <section class="hero">
      <div class="hero-main">
        <p class="hero-eyebrow">工作台</p>
        <h1 class="hero-title">{{ greeting }}，{{ nickname }}</h1>
        <p class="hero-sub">今天平台有 {{ stats.onSale }} 件商品在售，共 {{ stats.categories }} 个分类。想上新？先拍照做一次质检。</p>

        <div class="hero-search">
          <el-input
            v-model="keyword"
            placeholder="搜索商品：iPhone、MacBook、相机…"
            size="large"
            clearable
            class="search-input"
            @keyup.enter="handleSearch"
            @clear="resetSearch"
          >
            <template #prefix><el-icon><Search /></el-icon></template>
          </el-input>
          <el-button type="primary" size="large" @click="handleSearch">搜索</el-button>
        </div>
      </div>
      <div class="hero-actions">
        <el-button type="primary" size="large" @click="go('/trade/publish')">发布商品</el-button>
        <el-button size="large" @click="go('/trade/market')">逛逛市场</el-button>
      </div>
    </section>

    <!-- 数据 -->
    <section class="stats">
      <article class="stat">
        <span class="stat-label">在售商品</span>
        <strong class="stat-value">{{ stats.onSale }}</strong>
        <span class="stat-foot">全平台</span>
      </article>
      <article class="stat">
        <span class="stat-label">我的商品</span>
        <strong class="stat-value">{{ stats.mine }}</strong>
        <span class="stat-foot">含草稿与已售</span>
      </article>
      <article class="stat">
        <span class="stat-label">商品分类</span>
        <strong class="stat-value">{{ stats.categories }}</strong>
        <span class="stat-foot">三级分类</span>
      </article>
    </section>

    <!-- 商品 + 快捷入口 -->
    <section class="panels">
      <article class="panel">
        <header class="panel-head">
          <h2 class="panel-title">
            {{ searched ? `搜索“${activeKeyword}”的结果` : '在售好物' }}
            <em class="panel-count" v-if="searched">{{ goodsTotal }} 件</em>
          </h2>
          <div class="panel-head-actions">
            <button v-if="searched" type="button" class="panel-link" @click="resetSearch">清除搜索</button>
            <button type="button" class="panel-link" @click="go('/trade/market')">去市场看全部</button>
          </div>
        </header>

        <div v-loading="loading" class="goods">
          <div v-for="p in goods" :key="p.id" class="good" @click="go('/trade/detail?id=' + p.id)">
            <div class="good-cover">
              <img v-if="p.images && p.images.length" :src="p.images[0].url" alt="" />
              <i v-else class="thumb-letter">{{ (p.title || '·').slice(0, 1) }}</i>
              <span v-if="p.qualityGrade" class="good-grade">{{ p.qualityGrade }}级</span>
            </div>
            <div class="good-title">{{ p.title }}</div>
            <div class="good-meta">
              <span class="good-price">¥{{ formatPrice(p.price) }}</span>
              <span class="good-cat">{{ p.categoryName || '未分类' }}</span>
            </div>
          </div>

          <div v-if="!loading && goods.length === 0" class="empty">
            <p class="empty-title">{{ searched ? '没有找到相关商品' : '还没有在售商品' }}</p>
            <p class="empty-desc">{{ searched ? '换个关键词试试，或去二手市场看看。' : '发布第一件商品，拍照后 AI 会帮你定成色和参考价。' }}</p>
            <el-button type="primary" @click="searched ? resetSearch() : go('/trade/publish')">
              {{ searched ? '查看全部商品' : '去发布' }}
            </el-button>
          </div>
        </div>
      </article>

      <article class="panel side">
        <header class="panel-head">
          <h2 class="panel-title">快捷入口</h2>
        </header>

        <ul class="shortcuts">
          <li v-for="s in shortcuts" :key="s.path" @click="go(s.path)">
            <span class="sc-dot" :style="{ background: s.color }"></span>
            <span class="sc-text">
              <b>{{ s.title }}</b>
              <em>{{ s.desc }}</em>
            </span>
            <span class="sc-arrow">→</span>
          </li>
        </ul>

        <div class="tech">
          <p class="tech-title">技术栈</p>
          <div class="tech-tags">
            <span v-for="t in tech" :key="t">{{ t }}</span>
          </div>
        </div>
      </article>
    </section>
  </div>
</template>

<script setup name="Index" lang="ts">
import { Search } from '@element-plus/icons-vue';
import { listOnSaleProduct, listMyProduct } from '@/api/trade/product';
import { listCategoryTree } from '@/api/trade/category';
import { useUserStore } from '@/store/modules/user';
import type { ProductVO } from '@/api/trade/types';

const router = useRouter();
const userStore = useUserStore();

const nickname = computed(() => userStore.nickname || '同学');

const greeting = computed(() => {
  const h = new Date().getHours();
  if (h < 6) return '夜深了';
  if (h < 11) return '早上好';
  if (h < 14) return '中午好';
  if (h < 18) return '下午好';
  return '晚上好';
});

const stats = reactive({ onSale: 0, mine: 0, categories: 0 });
const goods = ref<ProductVO[]>([]);
const goodsTotal = ref(0);
const loading = ref(false);
const keyword = ref('');
const activeKeyword = ref('');
const searched = ref(false);

const shortcuts = [
  { title: '发布商品', desc: '拍照 → 质检 → 定价', path: '/trade/publish', color: 'var(--sm-brand)' },
  { title: '我的商品', desc: '管理在售与草稿', path: '/trade/my', color: 'var(--sm-accent)' },
  { title: '我的收藏', desc: '关注的设备', path: '/trade/favorite', color: '#f0b429' },
  { title: '二手市场', desc: '浏览全站商品', path: '/trade/market', color: '#9b8cff' }
];

const tech = ['Vue 3', 'Spring Boot 3', 'MyBatis Plus', 'PostgreSQL', 'Redis', 'RocketMQ', 'Sa-Token', 'AgentScope'];

const formatPrice = (v?: number) => (v == null ? '—' : Number(v).toLocaleString('zh-CN'));

const go = (path: string) => router.push(path);

const total = (res: any): number => res?.total ?? res?.data?.total ?? 0;
const rows = (res: any): ProductVO[] => res?.rows ?? res?.data?.rows ?? [];

/** 加载在售商品(keyword 为空时展示最新上架) */
const loadGoods = async (title?: string) => {
  loading.value = true;
  try {
    const res = await listOnSaleProduct({
      pageNum: 1,
      pageSize: 8,
      ...(title ? { title } : {})
    } as any);
    goods.value = rows(res);
    goodsTotal.value = total(res);
  } finally {
    loading.value = false;
  }
};

const handleSearch = async () => {
  const kw = keyword.value.trim();
  if (!kw) {
    resetSearch();
    return;
  }
  activeKeyword.value = kw;
  searched.value = true;
  await loadGoods(kw);
};

const resetSearch = async () => {
  keyword.value = '';
  activeKeyword.value = '';
  searched.value = false;
  await loadGoods();
};

onMounted(async () => {
  const [mine, cats] = await Promise.allSettled([
    listMyProduct({ pageNum: 1, pageSize: 1 } as any),
    listCategoryTree()
  ]);
  if (mine.status === 'fulfilled') {
    stats.mine = total(mine.value);
  }
  if (cats.status === 'fulfilled') {
    const list = (cats.value as any)?.data ?? cats.value;
    stats.categories = Array.isArray(list) ? list.length : 0;
  }
  // 在售商品与总数(默认最新上架 8 件)
  loading.value = true;
  try {
    const res = await listOnSaleProduct({ pageNum: 1, pageSize: 8 } as any);
    goods.value = rows(res);
    goodsTotal.value = total(res);
    stats.onSale = total(res);
  } finally {
    loading.value = false;
  }
});
</script>

<style lang="scss" scoped>
.dash {
  max-width: 1400px;
  margin: 0 auto;
  padding: 24px 28px 44px;
}

/* ---------- 问候 ---------- */
.hero {
  position: relative;
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 24px;
  padding: 26px 28px;
  border-radius: 18px;
  border: 1px solid var(--sm-card-border);
  background: var(--sm-paper);
  box-shadow: 0 1px 2px rgba(16, 45, 80, 0.04);
  overflow: hidden;
}

.hero::after {
  content: '';
  position: absolute;
  right: -60px;
  top: -90px;
  width: 300px;
  height: 300px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(43, 156, 240, 0.22), transparent 66%);
  pointer-events: none;
}

.hero-eyebrow {
  margin: 0 0 8px;
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.16em;
  color: var(--sm-brand);
}

.hero-title {
  margin: 0;
  font-size: 30px;
  font-weight: 800;
  letter-spacing: -0.03em;
  line-height: 1.2;
  color: var(--sm-ink);
}

.hero-sub {
  margin: 10px 0 0;
  font-size: 13.5px;
  font-weight: 500;
  color: var(--sm-ink-3);
  max-width: 46em;
}

.hero-search {
  position: relative;
  z-index: 1;
  display: flex;
  gap: 10px;
  margin-top: 18px;
  max-width: 520px;

  :deep(.el-input__wrapper) {
    border-radius: var(--sm-r-pill);
    padding-left: 16px;
  }
}

.hero-actions {
  display: flex;
  gap: 10px;
  flex-shrink: 0;
  position: relative;
  z-index: 1;
}

/* ---------- 数据卡 ---------- */
.stats {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
  margin-top: 16px;
}

.stat {
  display: flex;
  flex-direction: column;
  gap: 2px;
  padding: 16px 20px;
  border-radius: 16px;
  border: 1px solid var(--sm-card-border);
  background: var(--sm-paper);
  box-shadow: 0 1px 2px rgba(16, 45, 80, 0.04);
  transition:
    transform var(--sm-dur) var(--sm-ease),
    box-shadow var(--sm-dur) var(--sm-ease);

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 14px rgba(16, 45, 80, 0.07);
  }
}

.stat-label {
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.04em;
  color: var(--sm-stone);
}

.stat-value {
  font-family: var(--sm-font-num);
  font-size: 30px;
  font-weight: 800;
  letter-spacing: -0.03em;
  line-height: 1.15;
  margin-top: 2px;
  color: var(--sm-ink);
  font-variant-numeric: tabular-nums;
}

.stat-foot {
  font-size: 11.5px;
  font-weight: 600;
  color: var(--sm-stone);
}

/* ---------- 两栏面板 ---------- */
.panels {
  display: grid;
  grid-template-columns: minmax(0, 1.62fr) minmax(0, 1fr);
  gap: 16px;
  margin-top: 16px;
  align-items: start;
}

.panel {
  padding: 20px 22px 18px;
  border-radius: 18px;
  border: 1px solid var(--sm-card-border);
  background: var(--sm-paper);
  box-shadow: 0 1px 2px rgba(16, 45, 80, 0.04);
}

.panel-head {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.panel-title {
  margin: 0;
  font-size: 16px;
  font-weight: 800;
  letter-spacing: -0.01em;
  color: var(--sm-ink);

  .panel-count {
    margin-left: 8px;
    font-style: normal;
    font-size: 12px;
    font-weight: 700;
    color: var(--sm-stone);
  }
}

.panel-head-actions {
  display: flex;
  gap: 14px;
}

.panel-link {
  border: none;
  background: none;
  padding: 0;
  font-size: 12.5px;
  font-weight: 700;
  color: var(--sm-brand);
  cursor: pointer;
  transition: opacity var(--sm-dur) ease;

  &:hover {
    opacity: 0.7;
  }
}

/* ---------- 商品网格 ---------- */
.goods {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
  min-height: 120px;
}

.good {
  cursor: pointer;
  border-radius: var(--sm-r-sm);
  transition: transform var(--sm-dur) var(--sm-ease);

  &:hover {
    transform: translateY(-2px);

    .good-cover img {
      transform: scale(1.04);
    }

    .good-title {
      color: var(--sm-brand-deep);
    }
  }
}

.good-cover {
  position: relative;
  height: 110px;
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

.good-grade {
  position: absolute;
  top: 6px;
  right: 6px;
  font-family: var(--sm-font-num);
  font-size: 10.5px;
  font-weight: 800;
  color: var(--sm-brand-deep);
  background: rgba(255, 255, 255, 0.92);
  border-radius: var(--sm-r-pill);
  padding: 1px 8px;
}

.good-title {
  margin-top: 8px;
  font-size: 12.5px;
  font-weight: 700;
  color: var(--sm-ink);
  line-height: 1.4;
  height: 35px;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  transition: color var(--sm-dur) ease;
}

.good-meta {
  margin-top: 6px;
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 6px;
}

.good-price {
  font-family: var(--sm-font-num);
  font-size: 15px;
  font-weight: 800;
  color: var(--sm-danger);
  font-variant-numeric: tabular-nums;
}

.good-cat {
  font-size: 11px;
  font-weight: 600;
  color: var(--sm-stone);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 空状态 */
.empty {
  grid-column: 1 / -1;
  padding: 26px 8px 12px;
  text-align: center;
}

.empty-title {
  margin: 0;
  font-size: 14px;
  font-weight: 800;
  color: var(--sm-ink);
}

.empty-desc {
  margin: 6px 0 16px;
  font-size: 12.5px;
  font-weight: 500;
  color: var(--sm-stone);
}

/* 快捷入口 */
.shortcuts {
  list-style: none;
  margin: 6px 0 0;
  padding: 0;

  li {
    display: flex;
    align-items: center;
    gap: 10px;
    padding: 10px 8px;
    margin: 0 -8px;
    border-radius: var(--sm-r-sm);
    cursor: pointer;
    transition:
      background-color var(--sm-dur) ease,
      transform var(--sm-dur) var(--sm-ease);

    &:hover {
      background: var(--sm-brand-tint);
      transform: translateX(2px);

      .sc-arrow {
        opacity: 1;
        transform: translateX(2px);
      }
    }
  }
}

.sc-dot {
  width: 8px;
  height: 8px;
  border-radius: 3px;
  flex-shrink: 0;
}

.sc-text {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;

  b {
    font-size: 13px;
    font-weight: 700;
    color: var(--sm-ink);
  }

  em {
    font-style: normal;
    font-size: 11.5px;
    font-weight: 500;
    color: var(--sm-stone);
  }
}

.sc-arrow {
  color: var(--sm-brand);
  font-weight: 800;
  opacity: 0;
  transition: all var(--sm-dur) var(--sm-ease);
}

/* 技术栈 */
.tech {
  margin-top: 14px;
  padding-top: 14px;
  border-top: 1px solid var(--sm-line);
}

.tech-title {
  margin: 0 0 8px;
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.12em;
  color: var(--sm-stone);
}

.tech-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;

  span {
    font-size: 11px;
    font-weight: 700;
    color: var(--sm-ink-3);
    background: var(--sm-bg);
    border: 1px solid var(--sm-line-soft);
    border-radius: var(--sm-r-pill);
    padding: 3px 9px;
  }
}

/* ---------- 响应式 ---------- */
@media (max-width: 1180px) {
  .panels {
    grid-template-columns: minmax(0, 1fr);
  }

  .goods {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 860px) {
  .dash {
    padding: 18px 16px 36px;
  }

  .hero {
    flex-direction: column;
    align-items: flex-start;
    padding: 22px 20px;
  }

  .hero-title {
    font-size: 24px;
  }

  .stats {
    grid-template-columns: minmax(0, 1fr);
  }

  .goods {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}
</style>
