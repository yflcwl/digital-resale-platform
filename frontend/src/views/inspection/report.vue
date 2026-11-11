<template>
  <div class="app-container report-page" v-loading="loading">
    <button type="button" class="back" @click="goBack">
      <svg viewBox="0 0 16 16" aria-hidden="true">
        <path d="M13 8H4M7.5 4.5 4 8l3.5 3.5" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" />
      </svg>
      返回
    </button>

    <article v-if="report" class="doc">
      <!-- 文书头 -->
      <header class="doc-head">
        <span class="doc-brand">
          <i class="doc-mark"></i>
          洋葱智能质检
        </span>
        <span class="doc-no">报告编号 {{ report.id }}</span>
      </header>

      <!-- 结论区 -->
      <section class="verdict">
        <div class="grade-block" :class="`grade-${report.qualityGrade}`">
          <span class="grade-letter">{{ report.qualityGrade }}</span>
          <span class="grade-label">成色等级</span>
        </div>

        <div class="verdict-main">
          <h1 class="doc-title">{{ productTitle || '商品质检报告' }}</h1>

          <div class="scale" role="img" :aria-label="`成色 ${report.qualityGrade} 级，共五级`">
            <span v-for="g in ['S', 'A', 'B', 'C', 'D']" :key="g" :class="['scale-item', { on: g === report.qualityGrade }]">
              {{ g }}
            </span>
          </div>

          <dl class="facts">
            <div class="fact">
              <dt>识别机型</dt>
              <dd>{{ report.brandModel || '—' }}</dd>
            </div>
            <div class="fact">
              <dt>识别置信度</dt>
              <dd>{{ report.confidence ?? '—' }}%</dd>
            </div>
            <div class="fact">
              <dt>质检时间</dt>
              <dd>{{ report.createTime || '—' }}</dd>
            </div>
          </dl>
        </div>

        <div class="price">
          <span class="price-label">参考价格区间</span>
          <span class="price-value">¥{{ formatPrice(report.priceMin) }}<i>—</i>¥{{ formatPrice(report.priceMax) }}</span>
        </div>
      </section>

      <!-- 缺陷明细 -->
      <section class="defects">
        <h2 class="sec-title">缺陷明细<span class="count">{{ defects.length }}</span></h2>

        <p v-if="defects.length === 0" class="no-defect">未检出明显外观缺陷。</p>

        <ul v-else class="defect-list">
          <li v-for="(d, i) in defects" :key="i" class="defect-row">
            <span :class="['sev', `sev-${(d.severity || '').toLowerCase()}`]">{{ severityName(d.severity) }}</span>
            <span class="d-type">{{ defectTypeName(d.defectType) }}</span>
            <span class="d-pos">{{ d.position || '—' }}</span>
            <span class="d-desc">{{ d.description || '—' }}</span>
          </li>
        </ul>

        <p v-if="report.defectSummary" class="summary">{{ report.defectSummary }}</p>
      </section>

      <!-- 文书脚 -->
      <footer class="doc-foot">
        <p class="disclaimer">{{ report.disclaimer }}</p>
        <div class="actions">
          <button v-if="productId" type="button" class="btn-ghost" @click="goProduct">查看商品</button>
          <button type="button" class="btn-ghost" @click="openFeedback">报告反馈</button>
        </div>
      </footer>
    </article>

    <p v-else-if="!loading" class="empty">报告不存在或已被删除。</p>

    <!-- 反馈弹窗 -->
    <el-dialog v-model="feedbackVisible" title="报告反馈" width="440px" append-to-body>
      <el-input v-model="feedbackContent" type="textarea" :rows="3" placeholder="如:某处缺陷描述不准确、机型识别有误等" maxlength="500" show-word-limit />
      <template #footer>
        <el-button @click="feedbackVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitFeedback">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup name="InspectionReport" lang="ts">
import { onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { getReport, feedbackReport } from '@/api/inspection';
import { ElMessage } from 'element-plus';

const route = useRoute();
const router = useRouter();
const loading = ref(false);
const report = ref<any>();
const defects = ref<any[]>([]);
const productTitle = ref('');
const productId = ref<number>();

const feedbackVisible = ref(false);
const feedbackContent = ref('');
const submitting = ref(false);

const formatPrice = (v?: number) =>
  v == null ? '—' : Number(v).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 });

const defectTypeName = (type: string) => {
  const map: Record<string, string> = {
    SCRATCH: '划痕',
    CRACK: '裂纹',
    DENT: '磕碰',
    PAINT_LOSS: '掉漆',
    WATER_DAMAGE: '进水痕',
    OTHER: '其他'
  };
  return map[type] || type;
};

const severityName = (severity: string) => {
  const map: Record<string, string> = { MILD: '轻微', MODERATE: '中等', SEVERE: '严重' };
  return map[severity] || severity;
};

const load = async () => {
  loading.value = true;
  try {
    const res = await getReport(route.query.id as string);
    report.value = res.data.report;
    defects.value = res.data.defects || [];
    productTitle.value = res.data.productTitle || '';
    productId.value = res.data.productId;
  } finally {
    loading.value = false;
  }
};

const goProduct = () => {
  if (productId.value) {
    router.push('/trade/detail?id=' + productId.value);
  }
};

const goBack = () => {
  if (window.history.length > 1) {
    router.back();
  } else {
    router.push('/trade/my');
  }
};

const openFeedback = () => {
  feedbackContent.value = '';
  feedbackVisible.value = true;
};

const submitFeedback = async () => {
  if (!feedbackContent.value.trim()) {
    ElMessage.warning('请输入反馈内容');
    return;
  }
  submitting.value = true;
  try {
    await feedbackReport(report.value.id, feedbackContent.value.trim());
    ElMessage.success('反馈已提交,感谢你的帮助');
    feedbackVisible.value = false;
  } finally {
    submitting.value = false;
  }
};

onMounted(() => {
  load();
});
</script>

<style lang="scss" scoped>
.report-page {
  max-width: 920px;
}

/* ---------- 返回 ---------- */
.back {
  display: inline-flex;
  align-items: center;
  gap: 5px;
  margin-bottom: 16px;
  padding: 7px 13px 7px 9px;
  border: 1px solid var(--sm-card-border);
  border-radius: var(--sm-r-pill);
  background: var(--sm-paper);
  font-family: inherit;
  font-size: 12.5px;
  font-weight: 700;
  color: var(--sm-ink-2);
  cursor: pointer;
  transition:
    border-color 0.16s ease,
    color 0.16s ease;

  svg {
    width: 14px;
    height: 14px;
  }

  &:hover {
    border-color: var(--sm-line);
    color: var(--sm-ink);
  }
}

/* ---------- 文书面板 ---------- */
.doc {
  padding: 26px 30px 22px;
  border: 1px solid var(--sm-card-border);
  border-radius: 22px;
  background: var(--sm-paper);
}

.doc-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--sm-line-soft);
}

.doc-brand {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 12.5px;
  font-weight: 800;
  letter-spacing: 0.02em;
  color: var(--sm-ink-2);
}

.doc-mark {
  width: 10px;
  height: 10px;
  border-radius: 3px;
  background: var(--sm-brand);
}

.doc-no {
  font-family: var(--sm-font-num);
  font-size: 11.5px;
  font-weight: 600;
  color: var(--sm-stone);
}

/* ---------- 结论区 ---------- */
.verdict {
  display: grid;
  grid-template-columns: auto minmax(0, 1fr) auto;
  gap: 26px;
  align-items: center;
  padding: 24px 0 20px;
}

.grade-block {
  width: 108px;
  height: 108px;
  border-radius: 24px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 3px;
  color: #fff;

  .grade-letter {
    font-family: var(--sm-font-num);
    font-size: 42px;
    font-weight: 800;
    line-height: 1;
    text-shadow: 0 2px 8px rgba(0, 0, 0, 0.12);
  }

  .grade-label {
    font-size: 11.5px;
    font-weight: 600;
    opacity: 0.92;
  }

  /* 等级即语义：绿→蓝→琥珀→橙→红 */
  &.grade-S {
    background: linear-gradient(140deg, #2f9e6e, #4fc98a);
    box-shadow: 0 10px 24px -12px rgba(47, 158, 110, 0.55);
  }

  &.grade-A {
    background: linear-gradient(140deg, #0e6fb8, #2b9cf0);
    box-shadow: 0 10px 24px -12px rgba(14, 111, 184, 0.55);
  }

  &.grade-B {
    background: linear-gradient(140deg, #c98a0b, #f0b429);
    box-shadow: 0 10px 24px -12px rgba(201, 138, 11, 0.5);
  }

  &.grade-C {
    background: linear-gradient(140deg, #c77400, #e89b2f);
    box-shadow: 0 10px 24px -12px rgba(199, 116, 0, 0.5);
  }

  &.grade-D {
    background: linear-gradient(140deg, #a83228, #e0443a);
    box-shadow: 0 10px 24px -12px rgba(168, 50, 40, 0.5);
  }
}

.verdict-main {
  min-width: 0;
}

.doc-title {
  margin: 0;
  font-size: 22px;
  font-weight: 800;
  letter-spacing: -0.02em;
  line-height: 1.35;
  color: var(--sm-ink);
}

/* 五级刻度：当前等级高亮 */
.scale {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-top: 14px;
}

.scale-item {
  width: 30px;
  height: 24px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border-radius: 7px;
  background: var(--sm-bg-2);
  font-family: var(--sm-font-num);
  font-size: 12px;
  font-weight: 800;
  color: var(--sm-stone);
  transition: none;

  &.on {
    background: var(--sm-brand);
    color: #fff;
    box-shadow: 0 2px 0 var(--sm-brand-deep);
  }
}

.facts {
  display: flex;
  flex-wrap: wrap;
  gap: 6px 26px;
  margin: 16px 0 0;

  dt {
    font-size: 11.5px;
    font-weight: 600;
    color: var(--sm-stone);
  }

  dd {
    margin: 0 0 0 6px;
    display: inline;
    font-family: var(--sm-font-num);
    font-size: 13px;
    font-weight: 700;
    color: var(--sm-ink);
  }
}

/* 参考价：文书里最大的数字 */
.price {
  flex-shrink: 0;
  text-align: right;
}

.price-label {
  display: block;
  font-size: 11.5px;
  font-weight: 600;
  color: var(--sm-stone);
}

.price-value {
  display: block;
  margin-top: 8px;
  font-family: var(--sm-font-num);
  font-size: 26px;
  font-weight: 800;
  letter-spacing: -0.03em;
  color: var(--sm-ink);
  font-variant-numeric: tabular-nums;
  white-space: nowrap;

  i {
    margin: 0 6px;
    font-style: normal;
    color: var(--sm-line);
    font-weight: 400;
  }
}

/* ---------- 缺陷明细 ---------- */
.defects {
  border-top: 1px solid var(--sm-line-soft);
  padding-top: 20px;
}

.sec-title {
  margin: 0 0 14px;
  font-size: 15px;
  font-weight: 800;
  color: var(--sm-ink);
}

.count {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 20px;
  height: 20px;
  margin-left: 8px;
  padding: 0 6px;
  border-radius: var(--sm-r-pill);
  background: var(--sm-bg-2);
  font-family: var(--sm-font-num);
  font-size: 11.5px;
  font-weight: 800;
  color: var(--sm-ink-3);
  vertical-align: 2px;
}

.no-defect {
  margin: 0;
  padding: 18px 0;
  font-size: 13px;
  font-weight: 600;
  color: var(--sm-stone);
}

.defect-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin: 0;
  padding: 0;
  list-style: none;
}

.defect-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 16px;
  border: 1px solid var(--sm-line-soft);
  border-radius: 12px;
  background: var(--sm-bg);
}

.sev {
  flex-shrink: 0;
  min-width: 44px;
  padding: 2px 9px;
  border-radius: var(--sm-r-pill);
  font-size: 11px;
  font-weight: 800;
  text-align: center;

  &.sev-mild {
    background: var(--sm-accent-soft);
    color: #1c7a52;
  }

  &.sev-moderate {
    background: #fdf0dc;
    color: #9a6300;
  }

  &.sev-severe {
    background: #fdeceb;
    color: #a83228;
  }
}

.d-type {
  flex-shrink: 0;
  font-size: 13px;
  font-weight: 800;
  color: var(--sm-ink);
}

.d-pos {
  flex-shrink: 0;
  font-size: 12px;
  font-weight: 600;
  color: var(--sm-ink-3);
}

.d-desc {
  flex: 1;
  min-width: 0;
  font-size: 12.5px;
  font-weight: 500;
  line-height: 1.6;
  color: var(--sm-ink-2);
  text-align: right;
}

.summary {
  margin: 14px 0 0;
  padding: 12px 16px;
  border-radius: 12px;
  background: var(--sm-brand-tint);
  font-size: 12.5px;
  font-weight: 600;
  line-height: 1.7;
  color: var(--sm-ink-2);
}

/* ---------- 文书脚 ---------- */
.doc-foot {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 20px;
  margin-top: 22px;
  padding-top: 16px;
  border-top: 1px solid var(--sm-line-soft);
}

.disclaimer {
  margin: 0;
  max-width: 36em;
  font-size: 11.5px;
  font-weight: 500;
  line-height: 1.7;
  color: var(--sm-stone);
}

.actions {
  display: flex;
  flex-shrink: 0;
  gap: 10px;
}

.btn-ghost {
  height: 38px;
  padding: 0 20px;
  border: 1px solid var(--sm-line);
  border-radius: var(--sm-r-pill);
  background: var(--sm-paper);
  font-family: inherit;
  font-size: 13px;
  font-weight: 700;
  color: var(--sm-ink-2);
  cursor: pointer;
  transition:
    border-color 0.16s ease,
    color 0.16s ease;

  &:hover {
    border-color: var(--sm-brand);
    color: var(--sm-brand-deep);
  }
}

.empty {
  margin: 0;
  padding: 72px 0;
  text-align: center;
  font-size: 13.5px;
  font-weight: 600;
  color: var(--sm-stone);
}

/* ---------- 响应式 ---------- */
@media (max-width: 860px) {
  .verdict {
    grid-template-columns: auto minmax(0, 1fr);
  }

  .price {
    grid-column: 1 / -1;
    text-align: left;
    padding-top: 4px;
    border-top: 1px dashed var(--sm-line-soft);
  }

  .price-value {
    font-size: 22px;
  }

  .doc {
    padding: 20px 18px 18px;
  }

  .defect-row {
    flex-wrap: wrap;
  }

  .d-desc {
    flex: 1 1 100%;
    text-align: left;
  }

  .doc-foot {
    flex-direction: column;
    align-items: stretch;
  }
}
</style>
