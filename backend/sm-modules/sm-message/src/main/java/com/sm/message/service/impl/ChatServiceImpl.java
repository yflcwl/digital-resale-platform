package com.sm.message.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sm.common.core.exception.ServiceException;
import com.sm.common.core.utils.StringUtils;
import com.sm.common.json.utils.JsonUtils;
import com.sm.common.satoken.utils.LoginHelper;
import com.sm.common.websocket.utils.WebSocketUtils;
import com.sm.message.domain.ChatConversation;
import com.sm.message.domain.ChatMessage;
import com.sm.message.domain.vo.ConversationVo;
import com.sm.message.mapper.ChatConversationMapper;
import com.sm.message.mapper.ChatMessageMapper;
import com.sm.message.service.IChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 站内聊天Service业务层处理
 *
 * @author sm
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class ChatServiceImpl implements IChatService {

    /** 消息摘要截断长度 */
    private static final int PREVIEW_LENGTH = 100;

    private final ChatConversationMapper conversationMapper;
    private final ChatMessageMapper messageMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long openConversation(Long productId) {
        Long buyerId = LoginHelper.getUserId();
        Long sellerId = conversationMapper.selectProductSellerId(productId);
        if (sellerId == null) {
            throw new ServiceException("商品不存在");
        }
        if (buyerId.equals(sellerId)) {
            throw new ServiceException("不能与自己发起会话");
        }
        ChatConversation exist = conversationMapper.selectOne(Wrappers.<ChatConversation>lambdaQuery()
            .eq(ChatConversation::getBuyerId, buyerId)
            .eq(ChatConversation::getSellerId, sellerId)
            .eq(ChatConversation::getProductId, productId)
            .last("limit 1"));
        if (exist != null) {
            return exist.getId();
        }
        ChatConversation conversation = new ChatConversation();
        conversation.setBuyerId(buyerId);
        conversation.setSellerId(sellerId);
        conversation.setProductId(productId);
        conversation.setBuyerUnread(0);
        conversation.setSellerUnread(0);
        conversation.setStatus("0");
        conversation.setCreateTime(new Date());
        conversation.setUpdateTime(new Date());
        try {
            conversationMapper.insert(conversation);
        } catch (DuplicateKeyException e) {
            // 并发创建:返回已存在的会话
            ChatConversation again = conversationMapper.selectOne(Wrappers.<ChatConversation>lambdaQuery()
                .eq(ChatConversation::getBuyerId, buyerId)
                .eq(ChatConversation::getSellerId, sellerId)
                .eq(ChatConversation::getProductId, productId)
                .last("limit 1"));
            return again == null ? null : again.getId();
        }
        return conversation.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long send(Long conversationId, String msgType, String content, Long productId) {
        Long userId = LoginHelper.getUserId();
        if (StringUtils.isBlank(content)) {
            throw new ServiceException("消息内容不能为空");
        }
        if (content.length() > 1000) {
            throw new ServiceException("消息内容过长");
        }
        ChatConversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null) {
            throw new ServiceException("会话不存在");
        }
        boolean isBuyer = userId.equals(conversation.getBuyerId());
        boolean isSeller = userId.equals(conversation.getSellerId());
        if (!isBuyer && !isSeller) {
            throw new ServiceException("无权在该会话中发言");
        }
        Long receiverId = isBuyer ? conversation.getSellerId() : conversation.getBuyerId();

        ChatMessage message = new ChatMessage();
        message.setConversationId(conversationId);
        message.setSenderId(userId);
        message.setReceiverId(receiverId);
        message.setMsgType(StringUtils.isBlank(msgType) ? "TEXT" : msgType);
        message.setContent(content);
        message.setProductId(productId);
        message.setIsRead("0");
        message.setCreateTime(new Date());
        messageMapper.insert(message);

        LambdaUpdateWrapper<ChatConversation> uw = Wrappers.lambdaUpdate();
        uw.set(ChatConversation::getLastMessage, preview(content));
        uw.set(ChatConversation::getLastMsgTime, new Date());
        uw.set(ChatConversation::getUpdateTime, new Date());
        if (isBuyer) {
            uw.setSql("seller_unread = seller_unread + 1");
        } else {
            uw.setSql("buyer_unread = buyer_unread + 1");
        }
        uw.eq(ChatConversation::getId, conversationId);
        conversationMapper.update(null, uw);

        pushChatEvent(receiverId, conversationId, userId);
        return message.getId();
    }

    private String preview(String content) {
        String text = content;
        if (content.startsWith("{")) {
            try {
                Map<String, Object> json = JsonUtils.parseMap(content);
                Object preview = json.get("text");
                text = preview == null ? "[消息]" : String.valueOf(preview);
            } catch (Exception e) {
                text = "[消息]";
            }
        }
        return text.length() > PREVIEW_LENGTH ? text.substring(0, PREVIEW_LENGTH) : text;
    }

    private void pushChatEvent(Long receiverId, Long conversationId, Long senderId) {
        try {
            Map<String, Object> event = new LinkedHashMap<>();
            event.put("type", "chat");
            event.put("conversationId", conversationId);
            event.put("senderId", senderId);
            WebSocketUtils.sendMessage(receiverId, JsonUtils.toJsonString(event));
        } catch (Exception e) {
            // 推送失败不影响消息落库,前端可轮询补偿
            log.warn("聊天消息推送失败 receiverId={}", receiverId, e);
        }
    }

    @Override
    public List<ConversationVo> listConversations() {
        return conversationMapper.selectConversationList(LoginHelper.getUserId());
    }

    @Override
    public List<ChatMessage> listMessages(Long conversationId, long pageNum, long pageSize) {
        ChatConversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null) {
            throw new ServiceException("会话不存在");
        }
        Long userId = LoginHelper.getUserId();
        if (!userId.equals(conversation.getBuyerId()) && !userId.equals(conversation.getSellerId())) {
            throw new ServiceException("无权查看该会话");
        }
        Page<ChatMessage> page = messageMapper.selectPage(new Page<>(pageNum, pageSize),
            Wrappers.<ChatMessage>lambdaQuery()
                .eq(ChatMessage::getConversationId, conversationId)
                .orderByDesc(ChatMessage::getCreateTime));
        return page.getRecords();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markRead(Long conversationId) {
        ChatConversation conversation = conversationMapper.selectById(conversationId);
        if (conversation == null) {
            throw new ServiceException("会话不存在");
        }
        Long userId = LoginHelper.getUserId();
        boolean isBuyer = userId.equals(conversation.getBuyerId());
        if (!isBuyer && !userId.equals(conversation.getSellerId())) {
            throw new ServiceException("无权操作该会话");
        }
        LambdaUpdateWrapper<ChatConversation> uw = Wrappers.lambdaUpdate();
        if (isBuyer) {
            uw.set(ChatConversation::getBuyerUnread, 0);
        } else {
            uw.set(ChatConversation::getSellerUnread, 0);
        }
        uw.eq(ChatConversation::getId, conversationId);
        conversationMapper.update(null, uw);

        LambdaUpdateWrapper<ChatMessage> messageUw = Wrappers.lambdaUpdate();
        messageUw.set(ChatMessage::getIsRead, "1");
        messageUw.eq(ChatMessage::getConversationId, conversationId);
        messageUw.eq(ChatMessage::getReceiverId, userId);
        messageUw.eq(ChatMessage::getIsRead, "0");
        messageMapper.update(null, messageUw);
    }

    @Override
    public int unreadCount() {
        Long userId = LoginHelper.getUserId();
        List<ConversationVo> conversations = conversationMapper.selectConversationList(userId);
        return conversations.stream()
            .mapToInt(c -> c.getUnread() == null ? 0 : c.getUnread())
            .sum();
    }
}
