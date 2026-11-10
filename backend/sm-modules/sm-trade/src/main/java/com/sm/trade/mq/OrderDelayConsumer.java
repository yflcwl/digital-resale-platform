package com.sm.trade.mq;

import com.sm.common.json.utils.JsonUtils;
import com.sm.trade.service.IOrderService;
import com.sm.trade.service.impl.OrderServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 订单延迟消息消费者:超时未支付自动取消
 *
 * @author sm
 */
@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(topic = OrderServiceImpl.TOPIC_ORDER_DELAY, consumerGroup = "sm_order_consumer")
public class OrderDelayConsumer implements RocketMQListener<String> {

    private final IOrderService orderService;

    @Override
    public void onMessage(String message) {
        log.info("收到订单延迟消息: {}", message);
        Map<String, Object> body = JsonUtils.parseMap(message);
        Long orderId = Long.valueOf(String.valueOf(body.get("orderId")));
        orderService.handleTimeout(orderId);
    }
}
