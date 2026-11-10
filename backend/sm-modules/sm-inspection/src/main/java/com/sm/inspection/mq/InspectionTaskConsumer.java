package com.sm.inspection.mq;

import com.sm.common.json.utils.JsonUtils;
import com.sm.inspection.service.IInspectionService;
import com.sm.inspection.service.impl.InspectionServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 质检任务消息消费者:异步执行智能体质检流程
 *
 * @author sm
 */
@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(topic = InspectionServiceImpl.TOPIC_INSPECTION_TASK,
    consumerGroup = "sm_inspection_consumer")
public class InspectionTaskConsumer implements RocketMQListener<String> {

    private final IInspectionService inspectionService;

    @Override
    public void onMessage(String message) {
        log.info("收到质检任务消息: {}", message);
        Map<String, Object> body = JsonUtils.parseMap(message);
        Long taskId = Long.valueOf(String.valueOf(body.get("taskId")));
        inspectionService.executeTask(taskId);
    }
}
