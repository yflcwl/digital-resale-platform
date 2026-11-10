package com.sm.message.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 聊天会话对象 sm_chat_conversation
 *
 * @author sm
 */
@Data
@TableName("sm_chat_conversation")
public class ChatConversation implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(value = "id")
    private Long id;

    /** 买家ID */
    private Long buyerId;

    /** 卖家ID */
    private Long sellerId;

    /** 商品ID */
    private Long productId;

    /** 最后一条消息摘要 */
    private String lastMessage;

    /** 最后消息时间 */
    private Date lastMsgTime;

    /** 买家未读数 */
    private Integer buyerUnread;

    /** 卖家未读数 */
    private Integer sellerUnread;

    /** 状态(0正常 1已关闭) */
    private String status;

    /** 创建时间 */
    private Date createTime;

    /** 更新时间 */
    private Date updateTime;
}
