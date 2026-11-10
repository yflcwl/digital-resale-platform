<template>
  <div class="app-container">
    <el-card shadow="never">
      <template #header>
        <div class="header-bar">
          <span>智能质检</span>
          <el-tag v-if="taskStatus" :type="statusTagType" size="small">{{ statusText }}</el-tag>
        </div>
      </template>

      <el-alert
        type="info"
        :closable="false"
        show-icon
        title="AI 辅助质检流程:拍照合规校验 → 机型识别 → 缺陷分析 → 成色分级与参考定价"
        style="margin-bottom: 16px"
      />

      <!-- 拍照引导 -->
      <h4>拍照引导(请按以下角度拍摄)</h4>
      <el-row :gutter="12" class="guide-row">
        <el-col :span="4" v-for="(g, i) in guides" :key="g.angle">
          <el-card shadow="hover" class="guide-card">
            <div class="guide-index">{{ i + 1 }}</div>
            <div class="guide-angle">{{ g.angle }}</div>
            <div class="guide-tip">{{ g.tip }}</div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 照片上传 -->
      <h4 style="margin-top: 18px">上传照片(至少 3 张)</h4>
      <ImageUpload v-model="photoUrls" :limit="8" />

      <!-- 提交 -->
      <div class="action-bar">
        <el-button type="primary" size="large" :loading="submitting" :disabled="photoUrls.length < 3" @click="handleSubmit">
          {{ isReshoot ? '提交补拍照片' : '提交质检' }}
        </el-button>
        <el-button size="large" @click="goBack">返回</el-button>
      </div>

      <!-- 处理中 -->
      <div v-if="taskStatus === 'PROCESSING' || taskStatus === 'SUBMITTED'" class="processing">
        <el-icon class="is-loading" :size="28"><Loading /></el-icon>
        <span>智能体正在执行质检流程,请稍候(约 10~60 秒)...</span>
      </div>

      <!-- 需补拍 -->
      <el-alert
        v-if="taskStatus === 'NEED_RESHOOT'"
        type="warning"
        :closable="false"
        show-icon
        :title="task?.errorMsg || '照片不达标,请按引导补充拍摄后重新提交'"
        style="margin-top: 16px"
      />

      <!-- 失败 -->
      <el-alert
        v-if="taskStatus === 'FAILED'"
        type="error"
        :closable="false"
        show-icon
        :title="task?.errorMsg || '质检失败,请稍后重试'"
        style="margin-top: 16px"
      />
    </el-card>
  </div>
</template>

<script setup name="InspectionTask" lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { getGuide, submitInspection, reshootInspection, getTask } from '@/api/inspection';
import { ElMessage } from 'element-plus';

const route = useRoute();
const router = useRouter();

const productId = ref<number>();
const reshootTaskId = ref<number>();
const photoUrls = ref<string[]>([]);
const guides = ref<{ angle: string; tip: string }[]>([]);
const submitting = ref(false);
const taskId = ref<number>();
const taskStatus = ref<string>();
const task = ref<any>();
let pollTimer: number | undefined;

const isReshoot = computed(() => !!reshootTaskId.value);

const statusText = computed(() => {
  const map: Record<string, string> = {
    SUBMITTED: '已提交',
    PROCESSING: '质检中',
    SUCCESS: '质检完成',
    NEED_RESHOOT: '需补拍',
    FAILED: '质检失败'
  };
  return map[taskStatus.value || ''] || '';
});

const statusTagType = computed(() => {
  const map: Record<string, string> = {
    SUBMITTED: 'primary',
    PROCESSING: 'primary',
    SUCCESS: 'success',
    NEED_RESHOOT: 'warning',
    FAILED: 'danger'
  };
  return map[taskStatus.value || ''] || 'info';
});

const handleSubmit = async () => {
  if (photoUrls.value.length < 3) {
    ElMessage.warning('请至少上传 3 张照片');
    return;
  }
  submitting.value = true;
  try {
    const res = isReshoot.value
      ? await reshootInspection(reshootTaskId.value!, photoUrls.value)
      : await submitInspection(productId.value!, photoUrls.value);
    taskId.value = res.data;
    taskStatus.value = 'SUBMITTED';
    ElMessage.success('已提交,正在质检');
    startPolling();
  } finally {
    submitting.value = false;
  }
};

const poll = async () => {
  if (!taskId.value) return;
  try {
    const res = await getTask(taskId.value);
    task.value = res.data.task;
    taskStatus.value = task.value.status;
    if (taskStatus.value === 'SUCCESS') {
      stopPolling();
      ElMessage.success('质检完成');
      if (res.data.reportId) {
        router.push('/inspection/report?id=' + res.data.reportId);
      }
    } else if (taskStatus.value === 'NEED_RESHOOT' || taskStatus.value === 'FAILED') {
      stopPolling();
      photoUrls.value = [];
    }
  } catch {
    stopPolling();
  }
};

const startPolling = () => {
  stopPolling();
  pollTimer = window.setInterval(poll, 2000);
};

const stopPolling = () => {
  if (pollTimer) {
    window.clearInterval(pollTimer);
    pollTimer = undefined;
  }
};

const goBack = () => {
  if (window.history.length > 1) {
    router.back();
  } else {
    router.push('/trade/my');
  }
};

onMounted(async () => {
  productId.value = route.query.productId ? Number(route.query.productId) : undefined;
  reshootTaskId.value = route.query.taskId ? Number(route.query.taskId) : undefined;
  if (!productId.value && !reshootTaskId.value) {
    ElMessage.warning('缺少商品参数');
    return;
  }
  const res = await getGuide(0);
  guides.value = res.data;
  if (reshootTaskId.value) {
    const t = await getTask(reshootTaskId.value);
    task.value = t.data.task;
    taskStatus.value = 'NEED_RESHOOT';
    productId.value = Number(task.value.productId);
  }
});

onBeforeUnmount(() => {
  stopPolling();
});
</script>

<style lang="scss" scoped>
.header-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.guide-row {
  .guide-card {
    text-align: center;
    .guide-index {
      width: 26px;
      height: 26px;
      line-height: 26px;
      border-radius: 50%;
      margin: 0 auto 8px;
      background: var(--sm-brand);
      color: #fff;
      font-weight: 700;
      font-size: 13px;
    }
    .guide-angle {
      font-weight: 600;
    }
    .guide-tip {
      margin-top: 6px;
      color: var(--sm-ink-3);
      font-size: 12px;
      line-height: 1.5;
    }
  }
}
.action-bar {
  margin-top: 18px;
  display: flex;
  gap: 10px;
}
.processing {
  margin-top: 16px;
  display: flex;
  align-items: center;
  gap: 10px;
  color: var(--sm-brand);
}
</style>
