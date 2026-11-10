<template>
  <div class="app-container">
    <el-card shadow="never">
      <template #header>
        <div class="header-bar">
          <span>通知中心</span>
          <el-button type="primary" plain size="small" @click="handleReadAll">全部已读</el-button>
        </div>
      </template>

      <el-form :inline="true" @submit.prevent>
        <el-form-item>
          <el-select v-model="queryParams.type" placeholder="全部类型" clearable style="width: 150px" @change="handleQuery">
            <el-option v-for="t in notificationTypeOptions" :key="t.value" :label="t.label" :value="t.value" />
          </el-select>
        </el-form-item>
      </el-form>

      <el-empty v-if="!loading && list.length === 0" description="暂无通知" />

      <div v-loading="loading" class="notify-list">
        <div
          v-for="n in list"
          :key="n.id"
          class="notify-item"
          :class="{ unread: n.isRead === '0' }"
          @click="handleItemClick(n)"
        >
          <div class="notify-dot" v-if="n.isRead === '0'" />
          <div class="notify-main">
            <div class="notify-title">
              {{ n.title }}
              <el-tag size="small" :type="tagType(n.type)" effect="plain">{{ notificationTypeDict[n.type] || n.type }}</el-tag>
            </div>
            <div class="notify-content">{{ n.content }}</div>
          </div>
          <div class="notify-time">{{ n.createTime }}</div>
        </div>
      </div>

      <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
    </el-card>
  </div>
</template>

<script setup name="Notification" lang="ts">
import { onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { pageNotification, readNotification, readAllNotifications } from '@/api/message/notification';
import { NotificationVO, notificationTypeDict, notificationTypeOptions } from '@/api/message/types';
import { ElMessage } from 'element-plus';

const router = useRouter();
const loading = ref(false);
const total = ref(0);
const list = ref<NotificationVO[]>([]);

const queryParams = reactive<{ pageNum: number; pageSize: number; type?: string }>({
  pageNum: 1,
  pageSize: 10
});

const getList = async () => {
  loading.value = true;
  try {
    const res = await pageNotification(queryParams);
    list.value = res.rows;
    total.value = res.total;
  } finally {
    loading.value = false;
  }
};

const handleQuery = () => {
  queryParams.pageNum = 1;
  getList();
};

const tagType = (type: string) => {
  const map: Record<string, string> = { ORDER: 'primary', INSPECTION: 'warning', SYSTEM: 'info' };
  return map[type] || 'info';
};

const handleItemClick = async (n: NotificationVO) => {
  if (n.isRead === '0') {
    await readNotification(n.id);
    n.isRead = '1';
  }
  if (n.link) {
    router.push(n.link);
  }
};

const handleReadAll = async () => {
  await readAllNotifications();
  ElMessage.success('已全部标记为已读');
  getList();
};

onMounted(() => {
  getList();
});
</script>

<style lang="scss" scoped>
.header-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.notify-list {
  .notify-item {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 14px 12px;
    border-bottom: 1px solid var(--el-border-color-lighter);
    cursor: pointer;
    &:hover {
      background: var(--el-fill-color-light);
    }
    &.unread {
      background: var(--sm-brand-tint);
    }
    .notify-dot {
      width: 8px;
      height: 8px;
      border-radius: 50%;
      background: var(--sm-danger);
      flex-shrink: 0;
    }
    .notify-main {
      flex: 1;
      .notify-title {
        font-weight: 500;
        display: flex;
        align-items: center;
        gap: 8px;
      }
      .notify-content {
        margin-top: 4px;
        color: var(--sm-ink-3);
        font-size: 13px;
      }
    }
    .notify-time {
      color: var(--sm-stone);
      font-size: 12px;
      flex-shrink: 0;
    }
  }
}
</style>
