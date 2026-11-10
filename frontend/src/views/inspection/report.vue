<template>
  <div class="app-container" v-loading="loading">
    <el-button icon="ArrowLeft" @click="goBack" style="margin-bottom: 12px">返回</el-button>

    <template v-if="report">
      <el-card shadow="never" class="report-card">
        <div class="report-head">
          <div class="grade-block" :class="'grade-' + report.qualityGrade">
            <div class="grade-letter">{{ report.qualityGrade }}</div>
            <div class="grade-label">成色等级</div>
          </div>
          <div class="head-info">
            <h2>{{ productTitle || '商品质检报告' }}</h2>
            <div class="meta-row">
              <span>识别机型:{{ report.brandModel }}</span>
              <span>置信度:{{ report.confidence }}%</span>
              <span>质检时间:{{ report.createTime }}</span>
            </div>
            <div class="price-range">
              参考价格区间
              <span class="price-value">¥{{ report.priceMin }} ~ ¥{{ report.priceMax }}</span>
            </div>
          </div>
        </div>

        <el-divider />

        <h4>缺陷明细</h4>
        <el-empty v-if="!defects || defects.length === 0" description="未检出明显外观缺陷" :image-size="60" />
        <el-table v-else :data="defects" size="default">
          <el-table-column label="类型" width="120">
            <template #default="scope">{{ defectTypeName(scope.row.defectType) }}</template>
          </el-table-column>
          <el-table-column prop="position" label="位置" width="110" />
          <el-table-column label="严重程度" width="110">
            <template #default="scope">
              <el-tag :type="severityTagType(scope.row.severity)" size="small">{{ severityName(scope.row.severity) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="description" label="说明" min-width="200" />
        </el-table>

        <div class="summary">
          <span>{{ report.defectSummary }}</span>
        </div>

        <el-divider />

        <div class="footer-bar">
          <span class="disclaimer">⚠️ {{ report.disclaimer }}</span>
          <div class="actions">
            <el-button v-if="productId" @click="goProduct">查看商品</el-button>
            <el-button type="primary" plain @click="openFeedback">报告反馈</el-button>
          </div>
        </div>
      </el-card>
    </template>
    <el-empty v-else-if="!loading" description="报告不存在" />

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

const severityTagType = (severity: string) => {
  const map: Record<string, string> = { MILD: 'info', MODERATE: 'warning', SEVERE: 'danger' };
  return map[severity] || 'info';
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
.report-card {
  .report-head {
    display: flex;
    gap: 20px;
    align-items: center;
    .grade-block {
      width: 110px;
      height: 110px;
      border-radius: var(--sm-r-md);
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      color: #fff;
      flex-shrink: 0;
      .grade-letter {
        font-size: 44px;
        font-weight: 800;
        line-height: 1;
      }
      .grade-label {
        margin-top: 6px;
        font-size: 12px;
        opacity: 0.9;
      }
      &.grade-S {
        background: linear-gradient(135deg, var(--sm-success), var(--sm-accent));
      }
      &.grade-A {
        background: linear-gradient(135deg, var(--sm-brand), #62b4f5);
      }
      &.grade-B {
        background: linear-gradient(135deg, var(--sm-warning), #f0b429);
      }
      &.grade-C {
        background: linear-gradient(135deg, #d98a0b, #c77400);
      }
      &.grade-D {
        background: linear-gradient(135deg, var(--sm-danger), #c93a30);
      }
    }
    .head-info {
      h2 {
        margin: 0;
        font-size: 20px;
      }
      .meta-row {
        margin-top: 10px;
        display: flex;
        gap: 18px;
        color: var(--sm-ink-3);
        font-size: 13px;
      }
      .price-range {
        margin-top: 12px;
        color: var(--sm-ink-2);
        .price-value {
          margin-left: 8px;
          color: var(--sm-danger);
          font-size: 22px;
          font-weight: 700;
        }
      }
    }
  }
  .summary {
    margin-top: 10px;
    color: var(--sm-ink-3);
    font-size: 13px;
  }
  .footer-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    .disclaimer {
      color: var(--sm-ink-3);
      font-size: 12px;
    }
    .actions {
      display: flex;
      gap: 10px;
    }
  }
}
</style>
