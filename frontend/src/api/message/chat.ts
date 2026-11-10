import request from '@/utils/request';
import { AxiosPromise } from 'axios';
import { ChatMessageVO, ConversationVO } from './types';

/**
 * 从商品发起(或复用)会话
 */
export const openConversation = (productId: number | string): AxiosPromise<number> => {
  return request({
    url: '/message/chat/conversation',
    method: 'post',
    data: { productId }
  });
};

/**
 * 我的会话列表
 */
export const listConversations = (): AxiosPromise<ConversationVO[]> => {
  return request({
    url: '/message/chat/conversation/list',
    method: 'get'
  });
};

/**
 * 会话历史消息(分页,时间倒序)
 */
export const listMessages = (conversationId: number | string, pageNum = 1, pageSize = 20): AxiosPromise<ChatMessageVO[]> => {
  return request({
    url: `/message/chat/conversation/${conversationId}/messages`,
    method: 'get',
    params: { pageNum, pageSize }
  });
};

/**
 * 发送消息
 */
export const sendMessage = (
  conversationId: number | string,
  data: { msgType?: string; content: string; productId?: number | string }
): AxiosPromise<number> => {
  return request({
    url: `/message/chat/conversation/${conversationId}/send`,
    method: 'post',
    data
  });
};

/**
 * 会话已读
 */
export const markConversationRead = (conversationId: number | string): AxiosPromise<void> => {
  return request({
    url: `/message/chat/conversation/${conversationId}/read`,
    method: 'post'
  });
};

/**
 * 聊天未读总数
 */
export const chatUnreadCount = (): AxiosPromise<number> => {
  return request({
    url: '/message/chat/unread-count',
    method: 'get'
  });
};
