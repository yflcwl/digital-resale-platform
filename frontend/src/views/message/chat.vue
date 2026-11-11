<template>
  <div class="app-container chat-page">
    <div class="chat-shell">
      <!-- 左侧会话列表 -->
      <aside class="conv-panel">
        <header class="conv-head">
          <span class="conv-title">消息</span>
          <span class="conv-count">{{ conversations.length }}</span>
        </header>

        <p v-if="conversations.length === 0" class="conv-empty">还没有会话。<br />在商品页点「联系卖家」就会出现在这里。</p>

        <ul v-else class="conv-list">
          <li v-for="c in conversations" :key="c.id" :class="['conv-item', { active: c.id === activeId }]" @click="selectConversation(c)">
            <span class="conv-avatar">
              <img v-if="c.otherAvatar" :src="c.otherAvatar" alt="" />
              <i v-else>{{ (c.otherNickname || '对').charAt(0) }}</i>
              <b v-if="c.unread" class="conv-badge">{{ c.unread > 99 ? '99+' : c.unread }}</b>
            </span>

            <span class="conv-main">
              <span class="conv-top">
                <span class="conv-name">{{ c.otherNickname || '对方' }}</span>
                <span class="conv-time">{{ formatTime(c.lastMsgTime) }}</span>
              </span>
              <span class="conv-preview">{{ c.lastMessage || '开始聊聊吧' }}</span>
            </span>
          </li>
        </ul>
      </aside>

      <!-- 右侧聊天区 -->
      <section v-if="activeConversation" class="chat-panel">
        <header class="chat-head">
          <span class="chat-who">
            <span class="who-avatar">
              <img v-if="activeConversation.otherAvatar" :src="activeConversation.otherAvatar" alt="" />
              <i v-else>{{ (activeConversation.otherNickname || '对').charAt(0) }}</i>
            </span>
            <span class="who-name">{{ activeConversation.otherNickname || '对方' }}</span>
          </span>
          <span :class="['conn', wsConnected ? 'on' : 'off']">
            <i class="conn-dot"></i>
            {{ wsConnected ? '在线' : '连接中' }}
          </span>
        </header>

        <!-- 在谈商品 -->
        <button type="button" class="product-chip" @click="goProduct">
          <span class="chip-img">
            <img v-if="activeConversation.productImage" :src="activeConversation.productImage" alt="" />
          </span>
          <span class="chip-main">
            <span class="chip-title">正在聊：{{ activeConversation.productTitle }}</span>
            <span class="chip-price">¥{{ formatPrice(activeConversation.productPrice) }}</span>
          </span>
          <svg class="chip-arrow" viewBox="0 0 16 16" aria-hidden="true">
            <path d="M6 3.5 10.5 8 6 12.5" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round" stroke-linejoin="round" />
          </svg>
        </button>

        <!-- 消息流 -->
        <div ref="messageListRef" class="msg-list">
          <template v-for="(m, i) in messages" :key="m.id">
            <p v-if="showTimeDivider(i)" class="msg-day">{{ formatDay(m.createTime) }}</p>

            <div :class="['msg-row', { mine: isMine(m) }]">
              <span v-if="!isMine(m)" class="msg-avatar">
                <img v-if="activeConversation.otherAvatar" :src="activeConversation.otherAvatar" alt="" />
                <i v-else>{{ (activeConversation.otherNickname || '对').charAt(0) }}</i>
              </span>

              <div class="bubble" @dblclick="setQuote(m)">
                <div v-if="parseContent(m).quote" class="quote-block">
                  <span class="quote-name">{{ parseContent(m).quote.nickname }}</span>
                  <span class="quote-text">{{ parseContent(m).quote.preview }}</span>
                </div>

                <template v-if="m.msgType === 'IMAGE'">
                  <el-image
                    :src="parseContent(m).url"
                    fit="contain"
                    class="msg-image"
                    :preview-src-list="[parseContent(m).url]"
                    preview-teleported
                    hide-on-click-modal
                  />
                </template>
                <template v-else>{{ parseContent(m).text ?? m.content }}</template>
              </div>

              <button type="button" class="quote-btn" title="引用该消息" @click="setQuote(m)">
                <svg viewBox="0 0 16 16" aria-hidden="true">
                  <path d="M3 4h7M3 8h10M3 12h6" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" />
                </svg>
              </button>
            </div>
          </template>

          <p v-if="messages.length === 0" class="msg-empty">还没有消息，打个招呼吧。</p>
        </div>

        <!-- 输入区 -->
        <footer class="input-bar">
          <div v-if="quoteMessage" class="quote-preview">
            <span class="quote-preview-text">引用 {{ quoteMessage.nickname }}：{{ quoteMessage.preview }}</span>
            <button type="button" class="quote-cancel" @click="quoteMessage = null">取消</button>
          </div>

          <div class="input-row">
            <div class="tools">
              <el-popover placement="top-start" :width="336" trigger="click" popper-class="emoji-popper">
                <template #reference>
                  <button type="button" class="tool-btn" title="表情">
                    <svg viewBox="0 0 20 20" aria-hidden="true">
                      <circle cx="10" cy="10" r="7.6" fill="none" stroke="currentColor" stroke-width="1.5" />
                      <circle cx="7.2" cy="8.2" r="1.1" fill="currentColor" />
                      <circle cx="12.8" cy="8.2" r="1.1" fill="currentColor" />
                      <path d="M6.6 12c.9 1.3 2 2 3.4 2s2.5-.7 3.4-2" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" />
                    </svg>
                  </button>
                </template>
                <div class="emoji-panel">
                  <button v-for="e in emojis" :key="e" type="button" class="emoji-item" @click="appendEmoji(e)">{{ e }}</button>
                </div>
              </el-popover>

              <el-upload :show-file-list="false" :http-request="handleImageUpload" accept="image/*" class="tool-upload">
                <button type="button" class="tool-btn" title="发送图片" :disabled="uploading">
                  <svg viewBox="0 0 20 20" aria-hidden="true">
                    <rect x="2.5" y="4" width="15" height="12" rx="2.5" fill="none" stroke="currentColor" stroke-width="1.5" />
                    <circle cx="7.4" cy="8.6" r="1.5" fill="currentColor" />
                    <path d="M4 14.5 8.6 10l3 2.8 2.6-2.4 2.3 2.1" fill="none" stroke="currentColor" stroke-width="1.5" stroke-linejoin="round" />
                  </svg>
                </button>
              </el-upload>
            </div>

            <el-input
              v-model="inputText"
              type="textarea"
              :rows="2"
              resize="none"
              placeholder="输入消息，Enter 发送"
              maxlength="1000"
              @keydown.enter.exact.prevent="handleSend"
            />

            <button type="button" class="send-btn" :disabled="sending || !inputText.trim()" @click="handleSend">
              {{ sending ? '发送中' : '发送' }}
            </button>
          </div>
        </footer>
      </section>

      <section v-else class="chat-panel blank">
        <div class="blank-inner">
          <span class="blank-mark">
            <svg viewBox="0 0 24 24" aria-hidden="true">
              <path
                d="M4 6.5A2.5 2.5 0 0 1 6.5 4h11A2.5 2.5 0 0 1 20 6.5v8a2.5 2.5 0 0 1-2.5 2.5H9l-4.2 3.4c-.5.4-1.3 0-1.3-.7V6.5Z"
                fill="none"
                stroke="currentColor"
                stroke-width="1.6"
                stroke-linejoin="round"
              />
              <path d="M8 9h8M8 12.5h5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" />
            </svg>
          </span>
          <p class="blank-text">选择左侧会话开始聊天</p>
        </div>
      </section>
    </div>
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

const emojis = [
  '😀',
  '😄',
  '😁',
  '😆',
  '😊',
  '🙂',
  '😉',
  '😍',
  '🥰',
  '😘',
  '😜',
  '🤔',
  '😎',
  '🥳',
  '😢',
  '😭',
  '😤',
  '😡',
  '👍',
  '👎',
  '👌',
  '🙏',
  '💪',
  '🎉',
  '❤️',
  '🔥',
  '⭐',
  '💰',
  '📦',
  '🚚',
  '✅',
  '❌',
  '🤝',
  '😴',
  '🤣',
  '😂',
  '😇',
  '😅'
];

const myId = computed(() => Number(userStore.userId));
const activeConversation = computed(() => conversations.value.find((c) => c.id === activeId.value));

let ws: WebSocket | null = null;
let reconnectTimer: number | undefined;

const formatPrice = (v?: number) => (v == null ? '—' : Number(v).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 }));

const isMine = (m: ChatMessageVO) => Number(m.senderId) === myId.value;

const parseContent = (m: ChatMessageVO): { url?: string; text?: string; quote?: any } => {
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

/** 相邻两条消息间隔超过 10 分钟时，显示一条时间分隔 */
const showTimeDivider = (i: number) => {
  const cur = messages.value[i];
  if (!cur?.createTime) return false;
  if (i === 0) return true;
  const prev = messages.value[i - 1];
  if (!prev?.createTime) return false;
  const a = new Date(prev.createTime.replace(/-/g, '/')).getTime();
  const b = new Date(cur.createTime.replace(/-/g, '/')).getTime();
  if (Number.isNaN(a) || Number.isNaN(b)) return false;
  return b - a > 10 * 60 * 1000;
};

/** 分隔条用"人话"时间：今天/昨天 + 时分 */
const formatDay = (t?: string) => {
  if (!t) return '';
  const d = new Date(t.replace(/-/g, '/'));
  if (Number.isNaN(d.getTime())) return t;
  const hm = `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`;
  const now = new Date();
  if (d.toDateString() === now.toDateString()) return `今天 ${hm}`;
  const yesterday = new Date(now.getTime() - 86400000);
  if (d.toDateString() === yesterday.toDateString()) return `昨天 ${hm}`;
  return `${d.getMonth() + 1} 月 ${d.getDate()} 日 ${hm}`;
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
    try {
      const res = await openConversation(productId);
      await loadConversations();
      // id 是雪花数,超过 Number.MAX_SAFE_INTEGER,必须用字符串比较,转 Number 会丢精度
      const target = conversations.value.find((c) => String(c.id) === String(res.data));
      await selectConversation(target || conversations.value[0]);
    } catch {
      // 典型场景:访问自己发布的商品(不能与自己发起会话)。退回选中第一个会话
      await loadConversations();
      if (conversations.value.length) {
        await selectConversation(conversations.value[0]);
      }
    }
  } else if (conversationId) {
    const target = conversations.value.find((c) => String(c.id) === String(conversationId));
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
  /* 聊天页不需要滚动页面，容器自己撑满 */
  padding-bottom: 24px;
}

.chat-shell {
  display: flex;
  height: calc(100vh - 148px);
  min-height: 520px;
  border: 1px solid var(--sm-card-border);
  border-radius: 20px;
  background: var(--sm-paper);
  overflow: hidden;
}

/* ==================== 左侧会话 ==================== */
.conv-panel {
  flex: 0 0 288px;
  display: flex;
  flex-direction: column;
  border-right: 1px solid var(--sm-line-soft);
  background: var(--sm-bg);
}

.conv-head {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 16px 18px 12px;
}

.conv-title {
  font-size: 15px;
  font-weight: 800;
  letter-spacing: -0.01em;
  color: var(--sm-ink);
}

.conv-count {
  min-width: 20px;
  height: 20px;
  padding: 0 6px;
  border-radius: var(--sm-r-pill);
  background: var(--sm-bg-2);
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-family: var(--sm-font-num);
  font-size: 11.5px;
  font-weight: 800;
  color: var(--sm-ink-3);
}

.conv-empty {
  margin: 0;
  padding: 48px 22px;
  font-size: 12.5px;
  font-weight: 600;
  line-height: 1.9;
  color: var(--sm-stone);
}

.conv-list {
  flex: 1;
  margin: 0;
  padding: 0 8px 10px;
  list-style: none;
  overflow-y: auto;
}

.conv-item {
  display: flex;
  align-items: center;
  gap: 11px;
  margin-top: 2px;
  padding: 10px 10px;
  border-radius: 14px;
  cursor: pointer;
  transition: background-color 0.15s ease;

  &:hover {
    background: var(--sm-brand-tint);
  }

  /* 选中：淡蓝底 + 左侧蓝条，一眼锁定 */
  &.active {
    background: var(--sm-brand-soft);

    .conv-name {
      color: var(--sm-brand-deep);
    }
  }
}

.conv-avatar {
  position: relative;
  flex-shrink: 0;
  width: 42px;
  height: 42px;
  border-radius: 14px;
  overflow: hidden;
  background: var(--sm-bg-2);
  display: flex;
  align-items: center;
  justify-content: center;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
  }

  i {
    font-style: normal;
    font-size: 16px;
    font-weight: 800;
    color: var(--sm-brand-deep);
    opacity: 0.55;
  }
}

.conv-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  min-width: 17px;
  height: 17px;
  padding: 0 5px;
  border-radius: var(--sm-r-pill);
  background: var(--sm-danger);
  color: #fff;
  font-family: var(--sm-font-num);
  font-size: 10px;
  font-weight: 800;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 0 0 2px var(--sm-paper);
}

.conv-main {
  flex: 1;
  min-width: 0;
}

.conv-top {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 8px;
}

.conv-name {
  font-size: 13.5px;
  font-weight: 700;
  color: var(--sm-ink);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.conv-time {
  flex-shrink: 0;
  font-family: var(--sm-font-num);
  font-size: 11px;
  font-weight: 500;
  color: var(--sm-stone);
}

.conv-preview {
  display: block;
  margin-top: 3px;
  font-size: 12px;
  font-weight: 500;
  color: var(--sm-ink-3);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* ==================== 右侧聊天区 ==================== */
.chat-panel {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;

  &.blank {
    align-items: center;
    justify-content: center;
  }
}

.chat-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 13px 20px;
  border-bottom: 1px solid var(--sm-line-soft);
}

.chat-who {
  display: flex;
  align-items: center;
  gap: 10px;
}

.who-avatar {
  width: 34px;
  height: 34px;
  border-radius: 11px;
  overflow: hidden;
  background: var(--sm-bg-2);
  display: flex;
  align-items: center;
  justify-content: center;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
  }

  i {
    font-style: normal;
    font-size: 13px;
    font-weight: 800;
    color: var(--sm-brand-deep);
    opacity: 0.55;
  }
}

.who-name {
  font-size: 14.5px;
  font-weight: 800;
  color: var(--sm-ink);
}

.conn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 11.5px;
  font-weight: 700;
  color: var(--sm-stone);

  .conn-dot {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    background: var(--sm-line);
  }

  &.on {
    color: #1c7a52;

    .conn-dot {
      background: var(--sm-accent);
      box-shadow: 0 0 0 3px rgba(79, 201, 138, 0.16);
    }
  }
}

/* 在谈商品条 */
.product-chip {
  display: flex;
  align-items: center;
  gap: 11px;
  margin: 12px 20px 0;
  padding: 9px 12px;
  border: 1px solid var(--sm-line-soft);
  border-radius: 14px;
  background: var(--sm-bg);
  font-family: inherit;
  text-align: left;
  cursor: pointer;
  transition:
    border-color 0.16s ease,
    background-color 0.16s ease;

  &:hover {
    border-color: var(--sm-line);
    background: var(--sm-brand-tint);
  }
}

.chip-img {
  flex-shrink: 0;
  width: 38px;
  height: 38px;
  border-radius: 10px;
  overflow: hidden;
  background: var(--sm-brand-tint);

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
  }
}

.chip-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.chip-title {
  font-size: 12.5px;
  font-weight: 700;
  color: var(--sm-ink);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chip-price {
  font-family: var(--sm-font-num);
  font-size: 12px;
  font-weight: 800;
  color: var(--sm-brand-deep);
}

.chip-arrow {
  flex-shrink: 0;
  width: 14px;
  height: 14px;
  color: var(--sm-stone);
}

/* ==================== 消息流 ==================== */
.msg-list {
  flex: 1;
  overflow-y: auto;
  padding: 18px 20px 12px;
}

.msg-day {
  margin: 4px 0 12px;
  text-align: center;
  font-family: var(--sm-font-num);
  font-size: 10.5px;
  font-weight: 600;
  color: var(--sm-stone);
}

.msg-row {
  display: flex;
  align-items: flex-end;
  gap: 8px;
  margin-bottom: 12px;

  &.mine {
    flex-direction: row-reverse;

    .bubble {
      background: var(--sm-brand);
      color: #fff;
      border-color: transparent;
      border-bottom-right-radius: 6px;
    }

    .quote-btn {
      left: auto;
      right: 0;
      transform: translateX(-6px);
    }
  }
}

.msg-avatar {
  flex-shrink: 0;
  width: 30px;
  height: 30px;
  border-radius: 10px;
  overflow: hidden;
  background: var(--sm-bg-2);
  display: flex;
  align-items: center;
  justify-content: center;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
  }

  i {
    font-style: normal;
    font-size: 12px;
    font-weight: 800;
    color: var(--sm-brand-deep);
    opacity: 0.55;
  }
}

.bubble {
  position: relative;
  max-width: min(62%, 460px);
  padding: 9px 13px;
  border: 1px solid var(--sm-line-soft);
  border-radius: 16px;
  border-bottom-left-radius: 6px;
  background: var(--sm-paper);
  font-size: 13.5px;
  font-weight: 500;
  line-height: 1.65;
  color: var(--sm-ink);
  word-break: break-word;
}

.msg-image {
  max-width: 220px;
  max-height: 180px;
  border-radius: 10px;
  display: block;
}

/* 气泡内引用块 */
.quote-block {
  display: flex;
  flex-direction: column;
  gap: 1px;
  margin-bottom: 6px;
  padding: 5px 9px;
  border-left: 2.5px solid currentColor;
  border-radius: 6px;
  background: rgba(16, 27, 43, 0.05);
  opacity: 0.86;

  .quote-name {
    font-size: 11px;
    font-weight: 800;
  }

  .quote-text {
    font-size: 11.5px;
    font-weight: 500;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    max-width: 300px;
  }

  .mine & {
    background: rgba(255, 255, 255, 0.16);
    color: #fff;
  }
}

/* 引用按钮：hover 才浮现 */
.quote-btn {
  flex-shrink: 0;
  width: 26px;
  height: 26px;
  padding: 0;
  border: none;
  border-radius: 8px;
  background: transparent;
  color: var(--sm-stone);
  cursor: pointer;
  opacity: 0;
  transition:
    opacity 0.15s ease,
    background-color 0.15s ease,
    color 0.15s ease;

  svg {
    width: 14px;
    height: 14px;
  }

  .msg-row:hover & {
    opacity: 1;
  }

  &:hover {
    background: var(--sm-bg-2);
    color: var(--sm-brand-deep);
  }

  &:focus-visible {
    opacity: 1;
    outline: 2px solid var(--sm-brand);
  }
}

.msg-empty {
  margin: 0;
  padding: 48px 0;
  text-align: center;
  font-size: 12.5px;
  font-weight: 600;
  color: var(--sm-stone);
}

/* ==================== 输入区 ==================== */
.input-bar {
  border-top: 1px solid var(--sm-line-soft);
  padding: 10px 16px 14px;
}

.quote-preview {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 8px;
  padding: 7px 12px;
  border-radius: 10px;
  background: var(--sm-bg);

  .quote-preview-text {
    font-size: 12px;
    font-weight: 600;
    color: var(--sm-ink-3);
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .quote-cancel {
    flex-shrink: 0;
    padding: 0;
    border: none;
    background: none;
    font-family: inherit;
    font-size: 12px;
    font-weight: 700;
    color: var(--sm-brand-deep);
    cursor: pointer;
  }
}

.input-row {
  display: flex;
  align-items: flex-end;
  gap: 10px;
}

.tools {
  display: flex;
  align-items: center;
  gap: 4px;
  padding-bottom: 6px;
}

.tool-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  padding: 0;
  border: none;
  border-radius: 9px;
  background: transparent;
  color: var(--sm-ink-3);
  cursor: pointer;
  transition:
    background-color 0.15s ease,
    color 0.15s ease;

  svg {
    width: 18px;
    height: 18px;
  }

  &:hover {
    background: var(--sm-bg-2);
    color: var(--sm-brand-deep);
  }

  &:disabled {
    opacity: 0.5;
    cursor: wait;
  }
}

/* 输入框：圆角面板，去掉 EP 默认阴影 */
.input-row :deep(.el-textarea__inner) {
  flex: 1;
  border-radius: 14px;
  padding: 10px 14px;
  background: var(--sm-bg);
  box-shadow: none;
  font-size: 13.5px;
  line-height: 1.6;

  &:focus {
    background: var(--sm-paper);
    box-shadow: 0 0 0 1.5px var(--sm-brand) inset;
  }
}

.send-btn {
  flex-shrink: 0;
  height: 40px;
  padding: 0 22px;
  border: none;
  border-radius: 14px;
  background: var(--sm-brand);
  color: #fff;
  font-family: inherit;
  font-size: 13.5px;
  font-weight: 800;
  cursor: pointer;
  box-shadow: 0 2px 0 var(--sm-brand-deep);
  transition:
    transform 0.16s var(--sm-ease),
    box-shadow 0.16s var(--sm-ease),
    background-color 0.16s ease,
    opacity 0.16s ease;

  &:hover:not(:disabled) {
    background: #3aa6f2;
  }

  &:active:not(:disabled) {
    transform: translateY(2px);
    box-shadow: 0 0 0 var(--sm-brand-deep);
  }

  &:disabled {
    opacity: 0.45;
    cursor: not-allowed;
    box-shadow: none;
  }
}

/* ==================== 空态 ==================== */
.blank-inner {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 14px;
}

.blank-mark {
  width: 64px;
  height: 64px;
  border-radius: 22px;
  background: var(--sm-brand-soft);
  color: var(--sm-brand-deep);
  display: flex;
  align-items: center;
  justify-content: center;

  svg {
    width: 30px;
    height: 30px;
  }
}

.blank-text {
  margin: 0;
  font-size: 13px;
  font-weight: 600;
  color: var(--sm-stone);
}

/* ==================== 响应式 ==================== */
@media (max-width: 860px) {
  .chat-shell {
    height: calc(100vh - 128px);
  }

  .conv-panel {
    flex-basis: 232px;
  }
}

/* 尊重减少动效偏好 */
@media (prefers-reduced-motion: reduce) {
  .conv-item,
  .send-btn,
  .tool-btn,
  .method,
  .product-chip {
    transition: none;
  }
}
</style>
