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
              <div class="bubble" :class="{ mine: isMine(m) }" @dblclick="setQuote(m)">
                <div v-if="parseContent(m).quote" class="quote-block">
                  <div class="quote-name">{{ parseContent(m).quote.nickname }}</div>
                  <div class="quote-text">{{ parseContent(m).quote.preview }}</div>
                </div>
                <template v-if="m.msgType === 'IMAGE'">
                  <el-image :src="parseContent(m).url" fit="contain" class="msg-image" :preview-src-list="[parseContent(m).url]" preview-teleported />
                </template>
                <template v-else>{{ parseContent(m).text ?? m.content }}</template>
              </div>
              <el-tooltip content="引用该消息(双击气泡也可)">
                <el-button class="quote-btn" link type="primary" size="small" @click="setQuote(m)">引用</el-button>
              </el-tooltip>
            </div>
            <el-empty v-if="messages.length === 0" description="暂无消息,打个招呼吧" :image-size="60" />
          </div>

          <!-- 输入区 -->
          <div class="input-bar">
            <div class="input-panel">
              <div v-if="quoteMessage" class="quote-preview">
                <span class="quote-preview-text">
                  引用 {{ quoteMessage.nickname }}:{{ quoteMessage.preview }}
                </span>
                <el-button link type="danger" size="small" @click="quoteMessage = null">取消</el-button>
              </div>
              <div class="input-tools">
                <el-popover placement="top-start" :width="330" trigger="click">
                  <template #reference>
                    <el-button link class="tool-btn" title="表情">😊</el-button>
                  </template>
                  <div class="emoji-panel">
                    <span v-for="e in emojis" :key="e" class="emoji-item" @click="appendEmoji(e)">{{ e }}</span>
                  </div>
                </el-popover>
                <el-upload
                  :show-file-list="false"
                  :http-request="handleImageUpload"
                  accept="image/*"
                  class="tool-upload"
                >
                  <el-button link class="tool-btn" title="发送图片" :loading="uploading">🖼️</el-button>
                </el-upload>
              </div>
              <el-input
                v-model="inputText"
                type="textarea"
                :rows="2"
                resize="none"
                placeholder="输入消息,Enter 发送"
                maxlength="1000"
                @keydown.enter.exact.prevent="handleSend"
              />
            </div>
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
import request from '@/utils/request';
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
const uploading = ref(false);
const quoteMessage = ref<{ id: number; nickname: string; preview: string } | null>(null);

const emojis = ['😀','😄','😁','😆','😊','🙂','😉','😍','🥰','😘','😜','🤔','😎','🥳','😢','😭','😤','😡','👍','👎','👌','🙏','💪','🎉','❤️','🔥','⭐','💰','📦','🚚','✅','❌','🤝','😴','🤣','😂','😇','😅'];

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

const msgNickname = (m: ChatMessageVO) => (isMine(m) ? '我' : activeConversation.value?.otherNickname || '对方');

const msgPreview = (m: ChatMessageVO) => {
  if (m.msgType === 'IMAGE') return '[图片]';
  const parsed = parseContent(m);
  const text = parsed.text ?? '';
  return text.length > 30 ? text.substring(0, 30) + '...' : text;
};

const setQuote = (m: ChatMessageVO) => {
  quoteMessage.value = { id: m.id, nickname: msgNickname(m), preview: msgPreview(m) };
};

const appendEmoji = (e: string) => {
  inputText.value += e;
};

/** 发送图片:上传 OSS 后作为 IMAGE 消息发送 */
const handleImageUpload = async (options: any) => {
  const file: File = options.file;
  if (!activeId.value || !file) return;
  uploading.value = true;
  try {
    const formData = new FormData();
    formData.append('file', file);
    const res: any = await request({
      url: '/resource/oss/upload',
      method: 'post',
      data: formData,
      headers: { 'Content-Type': 'multipart/form-data' }
    });
    const url = res.data?.url;
    if (!url) {
      ElMessage.error('图片上传失败');
      return;
    }
    await sendMessage(activeId.value, { msgType: 'IMAGE', content: JSON.stringify({ url, text: '[图片]' }) });
    await loadMessages(activeId.value);
    await loadConversations();
  } catch {
    ElMessage.error('图片发送失败');
  } finally {
    uploading.value = false;
  }
};

const handleSend = async () => {
  if (!activeId.value || !inputText.value.trim()) return;
  sending.value = true;
  try {
    const payload: Record<string, any> = { text: inputText.value.trim() };
    if (quoteMessage.value) {
      payload.quote = quoteMessage.value;
    }
    await sendMessage(activeId.value, { msgType: 'TEXT', content: JSON.stringify(payload) });
    inputText.value = '';
    quoteMessage.value = null;
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
            color: var(--sm-ink-3);
            font-size: 12px;
          }
        }
        .conv-preview {
          margin-top: 4px;
          color: var(--sm-ink-3);
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
        background: var(--sm-line-soft);
        color: var(--sm-ink-3);
      }
      .banner-info {
        flex: 1;
        .banner-title {
          font-size: 13px;
        }
        .banner-price {
          color: var(--sm-danger);
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
          background: var(--sm-bg-2);
          line-height: 1.6;
          word-break: break-word;
          &.mine {
            background: var(--sm-brand);
            color: #fff;
          }
          .msg-image {
            max-width: 220px;
            max-height: 180px;
          }
          .quote-block {
            border-left: 3px solid var(--sm-stone);
            padding: 2px 8px;
            margin-bottom: 6px;
            background: rgba(0, 0, 0, 0.06);
            border-radius: 2px;
            .quote-name {
              font-size: 12px;
              font-weight: 600;
              color: var(--sm-ink-3);
            }
            .quote-text {
              font-size: 12px;
              color: var(--sm-ink-3);
            }
          }
        }
        .quote-btn {
          opacity: 0;
          align-self: center;
          transition: opacity 0.2s;
        }
        &:hover .quote-btn {
          opacity: 1;
        }
      }
    }
    .input-panel {
      flex: 1;
      min-width: 0;
    }
    .input-tools {
      display: flex;
      gap: 8px;
      margin-bottom: 4px;
      .tool-btn {
        font-size: 18px;
      }
    }
    .emoji-panel {
      display: flex;
      flex-wrap: wrap;
      gap: 2px;
      .emoji-item {
        font-size: 20px;
        cursor: pointer;
        padding: 3px;
        border-radius: 4px;
        &:hover {
          background: var(--sm-bg-2);
        }
      }
    }
    .quote-preview {
      display: flex;
      justify-content: space-between;
      align-items: center;
      background: var(--sm-line-soft);
      padding: 4px 10px;
      border-radius: 4px;
      margin-bottom: 6px;
      .quote-preview-text {
        font-size: 12px;
        color: var(--sm-ink-3);
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
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
