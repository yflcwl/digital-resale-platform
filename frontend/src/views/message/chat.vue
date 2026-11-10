<template>
  <div class="app-container chat-page">
    <el-card shadow="never" class="chat-card" :body-style="{ padding: 0, height: 'calc(100vh - 170px)' }">
      <div class="chat-layout">
        <!-- 左侧会话列表 -->
        <div class="conversation-panel">
          <div class="panel-header">消息</div>
          <el-empty v-if="conversations.length === 0" description="暂无会话" :image-size="70" />
          <ul v-else class="conv-list">
            <li
              v-for="c in conversations"
              :key="c.id"
              class="conv-item"
              :class="{ active: c.id === activeId }"
              @click="selectConversation(c)"
            >
              <el-badge :value="c.unread || 0" :hidden="!c.unread" :max="99">
                <el-avatar :size="40" :src="c.otherAvatar">{{ (c.otherNickname || '对').charAt(0) }}</el-avatar>
              </el-badge>
              <div class="conv-main">
                <div class="conv-top">
                  <span class="conv-name">{{ c.otherNickname || '对方' }}</span>
                  <span class="conv-time">{{ formatTime(c.lastMsgTime) }}</span>
                </div>
                <div class="conv-preview">{{ c.lastMessage || '开始聊聊吧' }}</div>
              </div>
            </li>
          </ul>
        </div>

        <!-- 右侧聊天区 -->
        <div class="chat-panel" v-if="activeConversation">
          <div class="chat-header">
            <div class="chat-title">
              {{ activeConversation.otherNickname || '对方' }}
              <el-tag v-if="wsConnected" type="success" size="small" effect="plain">在线</el-tag>
            </div>
          </div>

          <!-- 商品卡片 -->
          <div class="product-banner" @click="goProduct">
            <el-image :src="activeConversation.productImage" fit="cover" class="banner-img">
              <template #error><div class="img-slot"><el-icon><picture /></el-icon></div></template>
            </el-image>
            <div class="banner-info">
              <div class="banner-title">{{ activeConversation.productTitle }}</div>
              <div class="banner-price">¥{{ activeConversation.productPrice }}</div>
            </div>
            <el-button size="small" @click.stop="goProduct">查看商品</el-button>
          </div>

          <!-- 消息区 -->
          <div ref="messageListRef" class="message-list">
            <div v-for="m in messages" :key="m.id" class="message-row" :class="{ mine: isMine(m) }">
              <el-avatar v-if="!isMine(m)" :size="32" :src="activeConversation.otherAvatar">
                {{ (activeConversation.otherNickname || '对').charAt(0) }}
              </el-avatar>
              <div class="bubble" :class="{ mine: isMine(m) }">
                <template v-if="m.msgType === 'IMAGE'">
                  <el-image :src="parseContent(m).url" fit="contain" class="msg-image" :preview-src-list="[parseContent(m).url]" preview-teleported />
                </template>
                <template v-else>{{ parseContent(m).text ?? m.content }}</template>
              </div>
            </div>
            <el-empty v-if="messages.length === 0" description="暂无消息,打个招呼吧" :image-size="60" />
          </div>

          <!-- 输入区 -->
          <div class="input-bar">
            <el-input
              v-model="inputText"
              type="textarea"
              :rows="2"
              resize="none"
              placeholder="输入消息,Enter 发送"
              maxlength="1000"
              @keydown.enter.exact.prevent="handleSend"
            />
            <el-button type="primary" :loading="sending" @click="handleSend">发送</el-button>
          </div>
        </div>
        <div v-else class="chat-panel empty-panel">
          <el-empty description="选择左侧会话开始聊天" />
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup name="Chat" lang="ts">
import { computed, nextTick, onBeforeUnmount, onMounted, ref } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { listConversations, listMessages, sendMessage, markConversationRead, openConversation } from '@/api/message/chat';
import { ChatMessageVO, ConversationVO } from '@/api/message/types';
import { useUserStore } from '@/store/modules/user';
import { getToken } from '@/utils/auth';
import { ElMessage } from 'element-plus';

const route = useRoute();
const router = useRouter();
const userStore = useUserStore();

const conversations = ref<ConversationVO[]>([]);
const activeId = ref<number>();
const messages = ref<ChatMessageVO[]>([]);
const inputText = ref('');
const sending = ref(false);
const messageListRef = ref<HTMLElement>();
const wsConnected = ref(false);

const myId = computed(() => Number(userStore.userId));
const activeConversation = computed(() => conversations.value.find((c) => c.id === activeId.value));

let ws: WebSocket | null = null;
let reconnectTimer: number | undefined;

const isMine = (m: ChatMessageVO) => Number(m.senderId) === myId.value;

const parseContent = (m: ChatMessageVO): { url?: string; text?: string } => {
  if (m.msgType === 'IMAGE' || (m.content || '').startsWith('{')) {
    try {
      return JSON.parse(m.content);
    } catch {
      return { text: m.content };
    }
  }
  return { text: m.content };
};

const formatTime = (t?: string) => {
  if (!t) return '';
  const d = new Date(t.replace(/-/g, '/'));
  if (Number.isNaN(d.getTime())) return t;
  const now = new Date();
  if (d.toDateString() === now.toDateString()) {
    return `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
  }
  return `${d.getMonth() + 1}/${d.getDate()}`;
};

const loadConversations = async () => {
  const res = await listConversations();
  conversations.value = res.data || [];
};

const loadMessages = async (conversationId: number) => {
  const res = await listMessages(conversationId, 1, 100);
  messages.value = (res.data || []).slice().reverse();
  await nextTick();
  scrollToBottom();
};

const scrollToBottom = () => {
  if (messageListRef.value) {
    messageListRef.value.scrollTop = messageListRef.value.scrollHeight;
  }
};

const selectConversation = async (c: ConversationVO) => {
  activeId.value = c.id;
  c.unread = 0;
  await loadMessages(c.id);
  try {
    await markConversationRead(c.id);
  } catch {
    // 已读失败不阻断聊天
  }
};

const handleSend = async () => {
  if (!activeId.value || !inputText.value.trim()) return;
  sending.value = true;
  try {
    await sendMessage(activeId.value, { msgType: 'TEXT', content: inputText.value.trim() });
    inputText.value = '';
    await loadMessages(activeId.value);
    await loadConversations();
  } finally {
    sending.value = false;
  }
};

const goProduct = () => {
  if (activeConversation.value?.productId) {
    router.push('/trade/detail?id=' + activeConversation.value.productId);
  }
};

/* ---------- WebSocket ---------- */
const connectWs = () => {
  const token = getToken();
  if (!token) return;
  const baseApi = import.meta.env.VITE_APP_BASE_API;
  const clientId = import.meta.env.VITE_APP_CLIENT_ID;
  const protocol = location.protocol === 'https:' ? 'wss' : 'ws';
  const url = `${protocol}://${location.host}${baseApi}/websocket?token=${encodeURIComponent(token)}&clientid=${clientId}`;
  try {
    ws = new WebSocket(url);
    ws.onopen = () => {
      wsConnected.value = true;
    };
    ws.onclose = () => {
      wsConnected.value = false;
      // 断线重连(10 秒)
      reconnectTimer = window.setTimeout(connectWs, 10000);
    };
    ws.onerror = () => {
      wsConnected.value = false;
    };
    ws.onmessage = async (event) => {
      try {
        const data = JSON.parse(event.data);
        if (data.type === 'chat') {
          await loadConversations();
          if (data.conversationId === activeId.value) {
            await loadMessages(activeId.value);
            await markConversationRead(activeId.value);
          } else {
            ElMessage.info('收到新聊天消息');
          }
        }
      } catch {
        // 忽略非 JSON 消息
      }
    };
  } catch {
    wsConnected.value = false;
  }
};

onMounted(async () => {
  await loadConversations();
  // 从商品详情跳转:?productId=xx 自动发起会话
  const productId = route.query.productId as string;
  const conversationId = route.query.conversationId as string;
  if (productId) {
    const res = await openConversation(productId);
    await loadConversations();
    const target = conversations.value.find((c) => c.id === res.data);
    if (target) {
      await selectConversation(target);
    }
  } else if (conversationId) {
    const target = conversations.value.find((c) => c.id === Number(conversationId));
    if (target) {
      await selectConversation(target);
    }
  } else if (conversations.value.length > 0) {
    await selectConversation(conversations.value[0]);
  }
  connectWs();
});

onBeforeUnmount(() => {
  if (reconnectTimer) {
    window.clearTimeout(reconnectTimer);
  }
  ws?.close();
});
</script>

<style lang="scss" scoped>
.chat-page {
  .chat-card {
    overflow: hidden;
  }
  .chat-layout {
    display: flex;
    height: 100%;
  }
  .conversation-panel {
    width: 280px;
    border-right: 1px solid var(--el-border-color-lighter);
    display: flex;
    flex-direction: column;
    .panel-header {
      padding: 14px 16px;
      font-weight: 600;
      border-bottom: 1px solid var(--el-border-color-lighter);
    }
    .conv-list {
      list-style: none;
      margin: 0;
      padding: 0;
      overflow-y: auto;
      flex: 1;
    }
    .conv-item {
      display: flex;
      gap: 10px;
      padding: 12px 14px;
      cursor: pointer;
      border-bottom: 1px solid var(--el-border-color-lighter);
      &:hover {
        background: var(--el-fill-color-light);
      }
      &.active {
        background: var(--el-fill-color-light);
      }
      .conv-main {
        flex: 1;
        min-width: 0;
        .conv-top {
          display: flex;
          justify-content: space-between;
          .conv-name {
            font-weight: 500;
          }
          .conv-time {
            color: #909399;
            font-size: 12px;
          }
        }
        .conv-preview {
          margin-top: 4px;
          color: #909399;
          font-size: 12px;
          overflow: hidden;
          white-space: nowrap;
          text-overflow: ellipsis;
        }
      }
    }
  }
  .chat-panel {
    flex: 1;
    display: flex;
    flex-direction: column;
    min-width: 0;
    .chat-header {
      padding: 12px 16px;
      border-bottom: 1px solid var(--el-border-color-lighter);
      .chat-title {
        font-weight: 600;
        display: flex;
        align-items: center;
        gap: 8px;
      }
    }
    .product-banner {
      display: flex;
      align-items: center;
      gap: 10px;
      margin: 10px 16px 0;
      padding: 8px;
      background: var(--el-fill-color-light);
      border-radius: 6px;
      cursor: pointer;
      .banner-img {
        width: 44px;
        height: 44px;
        border-radius: 4px;
      }
      .img-slot {
        width: 100%;
        height: 100%;
        display: flex;
        align-items: center;
        justify-content: center;
        background: #eef1f6;
        color: #909399;
      }
      .banner-info {
        flex: 1;
        .banner-title {
          font-size: 13px;
        }
        .banner-price {
          color: #f56c6c;
          font-weight: 600;
          font-size: 13px;
        }
      }
    }
    .message-list {
      flex: 1;
      overflow-y: auto;
      padding: 16px;
      .message-row {
        display: flex;
        gap: 8px;
        margin-bottom: 14px;
        &.mine {
          flex-direction: row-reverse;
        }
        .bubble {
          max-width: 60%;
          padding: 8px 12px;
          border-radius: 8px;
          background: #f2f3f5;
          line-height: 1.6;
          word-break: break-word;
          &.mine {
            background: #409eff;
            color: #fff;
          }
          .msg-image {
            max-width: 220px;
            max-height: 180px;
          }
        }
      }
    }
    .input-bar {
      display: flex;
      gap: 10px;
      padding: 12px 16px;
      border-top: 1px solid var(--el-border-color-lighter);
      align-items: flex-end;
    }
  }
  .empty-panel {
    align-items: center;
    justify-content: center;
  }
}
</style>
