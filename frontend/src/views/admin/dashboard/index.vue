<template>
  <div class="app-container dashboard">
    <header class="sm-page-head">
      <div>
        <h1 class="sm-page-title">数据仪表盘</h1>
        <p class="sm-page-desc">平台整体情况：用户、商品、订单与质检的当日快照。</p>
      </div>
    </header>

    <section class="stats">
      <article v-for="card in cards" :key="card.label" class="stat">
        <p class="stat-label">{{ card.label }}</p>
        <p class="stat-value">{{ card.value ?? '—' }}</p>
        <span class="stat-mark" :style="{ background: card.color }"></span>
      </article>
    </section>

    <div class="charts">
      <section class="panel panel-wide">
        <h2 class="panel-title">近 7 天订单趋势</h2>
        <div ref="trendRef" class="chart"></div>
      </section>

      <section class="panel">
        <h2 class="panel-title">订单状态分布</h2>
        <div ref="statusRef" class="chart"></div>
      </section>
    </div>

    <section class="panel panel-full">
      <h2 class="panel-title">各品类商品数量</h2>
      <div ref="categoryRef" class="chart tall"></div>
    </section>
  </div>
</template>

<script setup name="Dashboard" lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue';
import * as echarts from 'echarts';
import { getDashboard } from '@/api/admin';

const data = ref<any>();
const trendRef = ref<HTMLElement>();
const statusRef = ref<HTMLElement>();
const categoryRef = ref<HTMLElement>();
let charts: echarts.ECharts[] = [];

/* 图表统一取色：与全站令牌同源，避免出现 ECharts 默认蓝 */
const INK = '#101b2b';
const INK_3 = '#8095ab';
const LINE = '#eaf2fb';
const BRAND = '#2b9cf0';
const PALETTE = ['#2b9cf0', '#4fc98a', '#f5b819', '#e0443a', '#7c6cf0', '#0e6fb8', '#8095ab'];

const cards = computed(() => [
  { label: '用户总数', value: data.value?.overview?.userCount, color: 'var(--sm-brand)' },
  { label: '商品总数', value: data.value?.overview?.productCount, color: 'var(--sm-accent)' },
  { label: '在售商品', value: data.value?.overview?.onSaleCount, color: '#7c6cf0' },
  { label: '订单总数', value: data.value?.overview?.orderCount, color: 'var(--sm-warning)' },
  { label: '今日订单', value: data.value?.overview?.todayOrderCount, color: 'var(--sm-danger)' },
  { label: '质检报告', value: data.value?.overview?.reportCount, color: 'var(--sm-stone)' }
]);

const statusName: Record<string, string> = {
  PENDING_PAY: '待支付',
  PAID: '已支付',
  SHIPPED: '已发货',
  RECEIVED: '已收货',
  REVIEWED: '已评价',
  FINISHED: '已完成',
  CANCELED: '已取消'
};

/** 状态色按语义给：待办=琥珀，进行中=蓝，完成=绿，取消=灰 */
const statusColor: Record<string, string> = {
  PENDING_PAY: '#f5b819',
  PAID: '#2b9cf0',
  SHIPPED: '#0e6fb8',
  RECEIVED: '#4fc98a',
  REVIEWED: '#2f9e6e',
  FINISHED: '#1c7a52',
  CANCELED: '#b8c7d8'
};

const axisCommon = {
  axisLine: { show: false },
  axisTick: { show: false },
  axisLabel: { color: INK_3, fontSize: 11, fontWeight: 600 }
};

const tipCommon = {
  backgroundColor: '#fff',
  borderColor: LINE,
  borderWidth: 1,
  padding: [8, 12],
  textStyle: { color: INK, fontSize: 12, fontWeight: 600 },
  extraCssText: 'box-shadow: 0 8px 24px -12px rgba(16,45,80,.3); border-radius: 10px;'
};

const render = () => {
  if (!data.value) return;
  const d = data.value;

  const trend = echarts.init(trendRef.value!);
  trend.setOption({
    color: [BRAND],
    tooltip: { trigger: 'axis', ...tipCommon },
    grid: { left: 8, right: 24, top: 18, bottom: 6, containLabel: true },
    xAxis: { type: 'category', boundaryGap: false, data: d.orderTrend.map((t: any) => t.day), ...axisCommon },
    yAxis: {
      type: 'value',
      minInterval: 1,
      ...axisCommon,
      splitLine: { lineStyle: { color: LINE, type: 'dashed' } }
    },
    series: [
      {
        name: '订单数',
        type: 'line',
        smooth: true,
        symbol: 'circle',
        symbolSize: 6,
        lineStyle: { width: 2.4 },
        itemStyle: { borderWidth: 2, borderColor: '#fff' },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(43,156,240,.22)' },
              { offset: 1, color: 'rgba(43,156,240,0)' }
            ]
          }
        },
        data: d.orderTrend.map((t: any) => t.cnt)
      }
    ]
  });

  const status = echarts.init(statusRef.value!);
  status.setOption({
    color: PALETTE,
    tooltip: { trigger: 'item', ...tipCommon },
    legend: {
      bottom: 0,
      icon: 'circle',
      itemWidth: 7,
      itemHeight: 7,
      itemGap: 12,
      textStyle: { color: INK_3, fontSize: 11, fontWeight: 600 }
    },
    series: [
      {
        type: 'pie',
        radius: ['46%', '68%'],
        center: ['50%', '44%'],
        avoidLabelOverlap: true,
        itemStyle: { borderColor: '#fff', borderWidth: 3 },
        label: { show: false },
        labelLine: { show: false },
        emphasis: { scale: true, scaleSize: 4 },
        data: d.orderStatusDist.map((s: any) => ({
          name: statusName[s.name] || s.name,
          value: s.value,
          itemStyle: { color: statusColor[s.name] || '#8095ab' }
        }))
      }
    ]
  });

  const category = echarts.init(categoryRef.value!);
  category.setOption({
    tooltip: { trigger: 'axis', ...tipCommon },
    grid: { left: 8, right: 12, top: 18, bottom: 6, containLabel: true },
    xAxis: { type: 'category', data: d.categoryDist.map((c: any) => c.name), ...axisCommon },
    yAxis: {
      type: 'value',
      minInterval: 1,
      ...axisCommon,
      splitLine: { lineStyle: { color: LINE, type: 'dashed' } }
    },
    series: [
      {
        type: 'bar',
        barMaxWidth: 38,
        itemStyle: { color: BRAND, borderRadius: [6, 6, 0, 0] },
        data: d.categoryDist.map((c: any) => c.value)
      }
    ]
  });

  charts = [trend, status, category];
};

const handleResize = () => charts.forEach((c) => c.resize());

let ro: ResizeObserver | undefined;

onMounted(async () => {
  const res = await getDashboard();
  data.value = res.data;

  // 容器宽度由 grid 决定，首帧可能还没算完；等布局稳定后再初始化，
  // 否则 canvas 会按旧宽度绘制，表现为环形图偏左、柱子偏窄。
  await nextTick();
  render();
  requestAnimationFrame(() => charts.forEach((c) => c.resize()));

  window.addEventListener('resize', handleResize);
  if (typeof ResizeObserver !== 'undefined') {
    ro = new ResizeObserver(() => charts.forEach((c) => c.resize()));
    [trendRef.value, statusRef.value, categoryRef.value].forEach((el) => el && ro!.observe(el));
  }
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize);
  ro?.disconnect();
  charts.forEach((c) => c.dispose());
});
</script>

<style lang="scss" scoped>
/* ---------- 概览 ---------- */
.stats {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 16px;
}

.stat {
  position: relative;
  padding: 16px 18px 14px;
  border: 1px solid var(--sm-card-border);
  border-radius: 16px;
  background: var(--sm-paper);
  overflow: hidden;
  transition: border-color 0.18s ease;

  &:hover {
    border-color: var(--sm-line);
  }
}

.stat-label {
  margin: 0;
  font-size: 12px;
  font-weight: 600;
  color: var(--sm-stone);
}

.stat-value {
  margin: 6px 0 0;
  font-family: var(--sm-font-num);
  font-size: 28px;
  font-weight: 800;
  line-height: 1.1;
  letter-spacing: -0.03em;
  color: var(--sm-ink);
  font-variant-numeric: tabular-nums;
}

/* 一条细竖线标识类别，比给数字上色克制 */
.stat-mark {
  position: absolute;
  top: 16px;
  right: 16px;
  width: 3px;
  height: 16px;
  border-radius: 3px;
  opacity: 0.85;
}

/* ---------- 图表 ---------- */
.charts {
  display: grid;
  grid-template-columns: minmax(0, 1.45fr) minmax(0, 1fr);
  gap: 14px;
}

.panel {
  padding: 18px 20px 12px;
  border: 1px solid var(--sm-card-border);
  border-radius: 18px;
  background: var(--sm-paper);
}

.panel-full {
  margin-top: 14px;
}

.panel-title {
  margin: 0 0 6px;
  font-size: 14px;
  font-weight: 800;
  letter-spacing: -0.01em;
  color: var(--sm-ink);
}

.chart {
  width: 100%;
  height: 268px;

  &.tall {
    height: 240px;
  }
}

/* ---------- 响应式 ---------- */
@media (max-width: 1200px) {
  .stats {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }

  .charts {
    grid-template-columns: minmax(0, 1fr);
  }
}

@media (max-width: 700px) {
  .stats {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .stat-value {
    font-size: 24px;
  }

  .chart {
    height: 220px;
  }
}
</style>
