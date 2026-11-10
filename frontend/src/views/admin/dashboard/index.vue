<template>
  <div class="app-container dashboard">
    <!-- 概览卡片 -->
    <el-row :gutter="16" class="stat-row">
      <el-col :span="4" v-for="card in cards" :key="card.label">
        <el-card shadow="hover" class="stat-card">
          <div class="stat-label">{{ card.label }}</div>
          <div class="stat-value" :style="{ color: card.color }">{{ card.value ?? '-' }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区 -->
    <el-row :gutter="16">
      <el-col :span="14">
        <el-card shadow="never">
          <template #header><span>近 7 天订单趋势</span></template>
          <div ref="trendRef" class="chart" style="height: 300px"></div>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card shadow="never">
          <template #header><span>订单状态分布</span></template>
          <div ref="statusRef" class="chart" style="height: 300px"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="16" style="margin-top: 16px">
      <el-col :span="24">
        <el-card shadow="never">
          <template #header><span>各品类商品数量</span></template>
          <div ref="categoryRef" class="chart" style="height: 300px"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup name="Dashboard" lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue';
import * as echarts from 'echarts';
import { getDashboard } from '@/api/admin';

const data = ref<any>();
const trendRef = ref<HTMLElement>();
const statusRef = ref<HTMLElement>();
const categoryRef = ref<HTMLElement>();
let charts: echarts.ECharts[] = [];

const cards = computed(() => [
  { label: '用户总数', value: data.value?.overview?.userCount, color: 'var(--sm-brand)' },
  { label: '商品总数', value: data.value?.overview?.productCount, color: '#22a06b' },
  { label: '在售商品', value: data.value?.overview?.onSaleCount, color: '#4fc98a' },
  { label: '订单总数', value: data.value?.overview?.orderCount, color: 'var(--sm-warning)' },
  { label: '今日订单', value: data.value?.overview?.todayOrderCount, color: 'var(--sm-danger)' },
  { label: '质检报告', value: data.value?.overview?.reportCount, color: '#7c6cf0' }
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

const render = () => {
  if (!data.value) return;
  const d = data.value;

  const trend = echarts.init(trendRef.value!);
  trend.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 40, right: 20, top: 30, bottom: 30 },
    xAxis: { type: 'category', data: d.orderTrend.map((t: any) => t.day) },
    yAxis: { type: 'value', minInterval: 1 },
    series: [
      {
        name: '订单数',
        type: 'line',
        smooth: true,
        areaStyle: { opacity: 0.15 },
        data: d.orderTrend.map((t: any) => t.cnt)
      }
    ]
  });

  const status = echarts.init(statusRef.value!);
  status.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [
      {
        type: 'pie',
        radius: ['40%', '68%'],
        label: { formatter: '{b}: {c}' },
        data: d.orderStatusDist.map((s: any) => ({ name: statusName[s.name] || s.name, value: s.value }))
      }
    ]
  });

  const category = echarts.init(categoryRef.value!);
  category.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 50, right: 20, top: 30, bottom: 30 },
    xAxis: { type: 'category', data: d.categoryDist.map((c: any) => c.name) },
    yAxis: { type: 'value', minInterval: 1 },
    series: [
      {
        type: 'bar',
        barMaxWidth: 40,
        itemStyle: { color: 'var(--sm-brand)', borderRadius: [4, 4, 0, 0] },
        data: d.categoryDist.map((c: any) => c.value)
      }
    ]
  });

  charts = [trend, status, category];
};

const handleResize = () => charts.forEach((c) => c.resize());

onMounted(async () => {
  const res = await getDashboard();
  data.value = res.data;
  render();
  window.addEventListener('resize', handleResize);
});

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize);
  charts.forEach((c) => c.dispose());
});
</script>

<style lang="scss" scoped>
.dashboard {
  .stat-row {
    margin-bottom: 16px;
    .stat-card {
      text-align: center;
      .stat-label {
        color: var(--sm-ink-3);
        font-size: 13px;
      }
      .stat-value {
        margin-top: 8px;
        font-size: 26px;
        font-weight: 700;
      }
    }
  }
}
</style>
