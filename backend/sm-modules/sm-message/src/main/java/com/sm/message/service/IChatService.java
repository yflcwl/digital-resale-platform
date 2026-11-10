package com.sm.message.service;

import com.sm.message.domain.ChatMessage;
import com.sm.message.domain.vo.ConversationVo;

import java.util.List;

/**
 * 站内聊天Service接口
 *
 * @author sm
 */
public interface IChatService {

    /**
     * 从商品发起(或复用)会话,返回会话ID
     */
    Long openConversation(Long productId);

    /**
     * 发送消息(落库 + 更新会话 + WebSocket 推送)
     */
    Long send(Long conversationId, String msgType, String content, Long productId);

    /**
     * 当前用户的会话列表
     */
    List<ConversationVo> listConversations();

    /**
     * 会话历史消息(分页,时间倒序)
     */
    List<ChatMessage> listMessages(Long conversationId, long pageNum, long pageSize);

    /**
     * 会话已读(清零当前用户未读)
     */
    void markRead(Long conversationId);

    /**
     * 当前用户聊天未读总数
     */
    int unreadCount();
}
