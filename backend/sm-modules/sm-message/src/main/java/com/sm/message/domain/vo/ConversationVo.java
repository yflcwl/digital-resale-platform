package com.sm.message.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 会话视图对象(含对方与商品信息)
 *
 * @author sm
 */
@Data
public class ConversationVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 会话ID */
    private Long id;

    /** 买家ID */
    private Long buyerId;

    /** 卖家ID */
    private Long sellerId;

    /** 商品ID */
    private Long productId;

    /** 最后消息 */
    private String lastMessage;

    /** 最后消息时间 */
    private Date lastMsgTime;

    /** 当前用户未读数 */
    private Integer unread;

    /** 对方用户ID */
    private Long otherUserId;

    /** 对方昵称 */
    private String otherNickname;

    /** 对方头像 */
    private String otherAvatar;

    /** 商品标题 */
    private String productTitle;

    /** 商品封面 */
    private String productImage;

    /** 商品价格 */
    private java.math.BigDecimal productPrice;
}
