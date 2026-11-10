package com.sm.trade.domain.vo;

import com.sm.trade.domain.Order;
import com.sm.trade.domain.OrderStatusLog;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.List;

/**
 * 订单视图对象
 *
 * @author sm
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class OrderVo extends Order {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 状态流转日志(详情) */
    private List<OrderStatusLog> statusLogs;

    /** 买家昵称 */
    private String buyerNickname;

    /** 卖家昵称 */
    private String sellerNickname;

    /** 商品标题(从快照解析,便于列表展示) */
    private String productTitle;

    /** 商品封面(从快照解析) */
    private String productImage;
}
