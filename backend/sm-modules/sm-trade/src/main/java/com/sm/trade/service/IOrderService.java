package com.sm.trade.service;

import com.sm.common.mybatis.core.page.PageQuery;
import com.sm.common.mybatis.core.page.TableDataInfo;
import com.sm.trade.domain.bo.OrderBo;
import com.sm.trade.domain.vo.OrderVo;

/**
 * 订单Service接口
 *
 * @author sm
 */
public interface IOrderService {

    /**
     * 创建订单(锁定商品 + 发送延迟消息)
     */
    Long create(OrderBo bo);

    /**
     * 模拟支付(幂等)
     */
    void pay(Long orderId, String payType);

    /**
     * 买家取消订单(仅待支付)
     */
    void cancel(Long orderId);

    /**
     * 卖家发货
     */
    void ship(Long orderId, String expressCompany, String expressNo);

    /**
     * 买家确认收货
     */
    void receive(Long orderId);

    /**
     * 买家评价
     */
    void review(Long orderId, Integer rating, String content);

    /**
     * 订单分页(role=buyer 我买到的 / seller 我卖出的)
     */
    TableDataInfo<OrderVo> queryMyPage(OrderBo bo, PageQuery pageQuery, String role);

    /**
     * 订单详情(含状态流转)
     */
    OrderVo queryDetail(Long orderId);

    /**
     * 超时取消(延迟消息消费调用,幂等)
     */
    void handleTimeout(Long orderId);
}
