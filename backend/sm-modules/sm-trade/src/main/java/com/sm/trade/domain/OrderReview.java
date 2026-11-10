package com.sm.trade.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 交易评价对象 sm_order_review
 *
 * @author sm
 */
@Data
@TableName("sm_order_review")
public class OrderReview implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(value = "id")
    private Long id;

    /** 订单ID */
    private Long orderId;

    /** 评价人ID */
    private Long reviewerId;

    /** 被评价人ID(卖家) */
    private Long revieweeId;

    /** 星级(1~5) */
    private Integer rating;

    /** 评价内容 */
    private String content;

    /** 评价时间 */
    private Date createTime;
}
