package com.sm.trade.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 订单状态流转日志对象 sm_order_status_log
 *
 * @author sm
 */
@Data
@TableName("sm_order_status_log")
public class OrderStatusLog implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(value = "id")
    private Long id;

    /** 订单ID */
    private Long orderId;

    /** 原状态 */
    private String fromStatus;

    /** 新状态 */
    private String toStatus;

    /** 操作方(BUYER/SELLER/SYSTEM) */
    private String operatorType;

    /** 操作人ID */
    private Long operatorId;

    /** 备注(如取消原因) */
    private String remark;

    /** 变更时间 */
    private Date createTime;
}
