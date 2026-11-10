package com.sm.message.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 聊天消息对象 sm_chat_message
 *
 * @author sm
 */
@Data
@TableName("sm_chat_message")
public class ChatMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(value = "id")
    private Long id;

    /** 会话ID */
    private Long conversationId;

    /** 发送人ID */
    private Long senderId;

    /** 接收人ID */
    private Long receiverId;

    /** 消息类型(TEXT/IMAGE/PRODUCT_CARD/BARGAIN) */
    private String msgType;

    /** 消息内容 */
    private String content;

    /** 引用商品ID(商品卡片消息) */
    private Long productId;

    /** 是否已读(0否 1是) */
    private String isRead;

    /** 发送时间 */
    private Date createTime;
}
