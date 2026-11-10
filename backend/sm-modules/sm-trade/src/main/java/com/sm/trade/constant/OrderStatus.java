package com.sm.trade.constant;

/**
 * 订单状态常量
 *
 * @author sm
 */
public interface OrderStatus {

    /** 待支付 */
    String PENDING_PAY = "PENDING_PAY";

    /** 已支付 */
    String PAID = "PAID";

    /** 已发货 */
    String SHIPPED = "SHIPPED";

    /** 已收货 */
    String RECEIVED = "RECEIVED";

    /** 已评价 */
    String REVIEWED = "REVIEWED";

    /** 已完成 */
    String FINISHED = "FINISHED";

    /** 已取消 */
    String CANCELED = "CANCELED";
}
