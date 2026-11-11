package com.sm.trade.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 卖家评价视图对象
 *
 * @author sm
 */
@Data
public class ReviewVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 评价ID */
    private Long id;

    /** 订单ID */
    private Long orderId;

    /** 星级 */
    private Integer rating;

    /** 内容 */
    private String content;

    /** 评价时间 */
    private Date createTime;

    /** 评价人昵称 */
    private String reviewerNickname;

    /** 评价人头像 */
    private String reviewerAvatar;
}
