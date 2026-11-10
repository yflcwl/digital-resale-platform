package com.sm.message.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.sm.common.core.domain.R;
import com.sm.common.idempotent.annotation.RepeatSubmit;
import com.sm.message.domain.ChatMessage;
import com.sm.message.domain.vo.ConversationVo;
import com.sm.message.service.IChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 站内聊天Controller
 *
 * @author sm
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/message/chat")
public class ChatController {

    private final IChatService chatService;

    /**
     * 从商品发起(或复用)会话
     */
    @SaCheckLogin
    @PostMapping("/conversation")
    public R<Long> openConversation(@RequestBody Map<String, Long> body) {
        return R.ok(chatService.openConversation(body.get("productId")));
    }

    /**
     * 我的会话列表
     */
    @SaCheckLogin
    @GetMapping("/conversation/list")
    public R<List<ConversationVo>> listConversations() {
        return R.ok(chatService.listConversations());
    }

    /**
     * 会话历史消息(分页,时间倒序)
     */
    @SaCheckLogin
    @GetMapping("/conversation/{id}/messages")
    public R<List<ChatMessage>> listMessages(@PathVariable Long id,
                                             @RequestParam(defaultValue = "1") long pageNum,
                                             @RequestParam(defaultValue = "20") long pageSize) {
        return R.ok(chatService.listMessages(id, pageNum, pageSize));
    }

    /**
     * 发送消息
     */
    @SaCheckLogin
    @RepeatSubmit
    @PostMapping("/conversation/{id}/send")
    public R<Long> send(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String msgType = body.get("msgType") == null ? "TEXT" : String.valueOf(body.get("msgType"));
        String content = body.get("content") == null ? null : String.valueOf(body.get("content"));
        Long productId = body.get("productId") == null ? null : Long.valueOf(String.valueOf(body.get("productId")));
        return R.ok(chatService.send(id, msgType, content, productId));
    }

    /**
     * 会话已读
     */
    @SaCheckLogin
    @PostMapping("/conversation/{id}/read")
    public R<Void> markRead(@PathVariable Long id) {
        chatService.markRead(id);
        return R.ok();
    }

    /**
     * 聊天未读总数
     */
    @SaCheckLogin
    @GetMapping("/unread-count")
    public R<Integer> unreadCount() {
        return R.ok(chatService.unreadCount());
    }
}
