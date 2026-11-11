<template>
  <div class="app-container notify">
    <header class="sm-page-head">
      <div>
        <h1 class="sm-page-title">通知中心</h1>
        <p class="sm-page-desc">订单流转、质检结果和系统公告都会推到这里。</p>
      </div>
      <el-button plain @click="handleReadAll">全部标为已读</el-button>
    </header>

    <div class="bar">
      <button type="button" :class="['pill', { on: !queryParams.type }]" @click="pickType(undefined)">全部</button>
      <button
        v-for="t in notificationTypeOptions"
        :key="t.value"
        type="button"
        :class="['pill', { on: queryParams.type === t.value }]"
        @click="pickType(t.value)"
      >
        {{ t.label }}
      </button>
    </div>

    <div v-loading="loading" class="list">
      <article
        v-for="n in list"
        :key="n.id"
        :class="['item', { unread: n.isRead === '0' }]"
        role="button"
        tabindex="0"
        @click="handleItemClick(n)"
        @keydown.enter="handleItemClick(n)"
      >
        <div class="i-main">
          <p class="i-title">
            <span v-if="n.isRead === '0'" class="i-dot" aria-label="未读"></span>
            {{ n.title }}
          </p>
          <p class="i-content">{{ n.content }}</p>
        </div>

        <div class="i-side">
          <span :class="['i-type', `is-${(n.type || '').toLowerCase()}`]">{{ notificationTypeDict[n.type] || n.type }}</span>
          <span class="i-time">{{ n.createTime }}</span>
        </div>
      </article>

      <p v-if="!loading && list.length === 0" class="empty">这里还没有消息。</p>
    </div>

    <pagination v-show="total > 0" v-model:page="queryParams.pageNum" v-model:limit="queryParams.pageSize" :total="total" @pagination="getList" />
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

const pickType = (t?: string) => {
  queryParams.type = t;
  queryParams.pageNum = 1;
  getList();
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
.notify {
  max-width: 980px;
}

/* ---------- 类型筛选：文字胶囊 ---------- */
.bar {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 14px;
}

.pill {
  padding: 6px 15px;
  border: 1px solid transparent;
  border-radius: var(--sm-r-pill);
  background: transparent;
  font-family: inherit;
  font-size: 13px;
  font-weight: 700;
  color: var(--sm-ink-2);
  cursor: pointer;
  transition:
    background-color 0.16s ease,
    color 0.16s ease,
    border-color 0.16s ease;

  &:hover {
    background: var(--sm-bg-2);
  }

  &.on {
    background: var(--sm-paper);
    border-color: var(--sm-line);
    color: var(--sm-brand-deep);
  }
}

/* ---------- 通知列表 ---------- */
.list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-height: 120px;
}

.item {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  padding: 15px 18px;
  border: 1px solid var(--sm-card-border);
  border-radius: 14px;
  background: var(--sm-paper);
  cursor: pointer;
  transition:
    border-color 0.16s ease,
    background-color 0.16s ease;

  &:hover {
    border-color: var(--sm-line);
  }

  &:focus-visible {
    outline: 2px solid var(--sm-brand);
    outline-offset: 2px;
  }

  /* 未读：左侧一道蓝条 + 极淡蓝底 */
  &.unread {
    background: linear-gradient(90deg, var(--sm-brand-tint) 0%, var(--sm-paper) 46%);
    border-color: var(--sm-brand-soft);
  }
}

.i-main {
  flex: 1;
  min-width: 0;
}

.i-title {
  display: flex;
  align-items: center;
  gap: 7px;
  margin: 0;
  font-size: 13.5px;
  font-weight: 700;
  color: var(--sm-ink);
}

.i-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: var(--sm-brand);
  flex-shrink: 0;
}

.i-content {
  margin: 5px 0 0;
  font-size: 12.5px;
  font-weight: 500;
  line-height: 1.7;
  color: var(--sm-ink-3);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.i-side {
  flex: 0 0 auto;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 6px;
}

.i-type {
  padding: 2px 9px;
  border-radius: var(--sm-r-pill);
  background: var(--sm-bg-2);
  color: var(--sm-ink-3);
  font-size: 10.5px;
  font-weight: 700;

  &.is-order {
    background: var(--sm-brand-soft);
    color: var(--sm-brand-deep);
  }

  &.is-inspection {
    background: #fdf0dc;
    color: #9a6300;
  }

  &.is-system {
    background: var(--sm-bg-2);
    color: var(--sm-ink-3);
  }
}

.i-time {
  font-family: var(--sm-font-num);
  font-size: 11px;
  font-weight: 500;
  color: var(--sm-stone);
  font-variant-numeric: tabular-nums;
}

.empty {
  margin: 0;
  padding: 64px 0;
  text-align: center;
  font-size: 13px;
  font-weight: 600;
  color: var(--sm-stone);
}

@media (max-width: 640px) {
  .item {
    flex-direction: column;
    gap: 8px;
  }

  .i-side {
    flex-direction: row;
    align-items: center;
    gap: 10px;
  }
}
</style>
