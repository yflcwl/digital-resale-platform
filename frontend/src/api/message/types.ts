/**
 * 会话 VO
 */
export interface ConversationVO {
  id: number;
  buyerId: number;
  sellerId: number;
  productId: number;
  lastMessage?: string;
  lastMsgTime?: string;
  unread?: number;
  otherUserId?: number;
  otherNickname?: string;
  otherAvatar?: string;
  productTitle?: string;
  productImage?: string;
  productPrice?: number;
}

/**
 * 聊天消息 VO
 */
export interface ChatMessageVO {
  id: number;
  conversationId: number;
  senderId: number;
  receiverId: number;
  msgType: string;
  content: string;
  productId?: number;
  isRead?: string;
  createTime?: string;
}

/**
 * 通知 VO
 */
export interface NotificationVO {
  id: number;
  userId: number;
  type: string;
  title: string;
  content?: string;
  link?: string;
  isRead?: string;
  readTime?: string;
  createTime?: string;
}

/**
 * 通知类型字典
 */
export const notificationTypeDict: Record<string, string> = {
  ORDER: '订单',
  INSPECTION: '质检',
  SYSTEM: '系统'
};

export const notificationTypeOptions = [
  { label: '订单', value: 'ORDER' },
  { label: '质检', value: 'INSPECTION' },
  { label: '系统', value: 'SYSTEM' }
];
