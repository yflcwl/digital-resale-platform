package com.sm.inspection.config;

import com.sm.inspection.perception.DashScopeVisionClient;
import com.sm.inspection.perception.VisionClient;
import io.agentscope.core.model.Model;
import io.agentscope.extensions.model.dashscope.DashScopeChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 质检模型装配:接入通义千问(DashScope)
 *
 * <p>Agent 编排与视觉感知统一使用同一个多模态模型(qwen3-vl-plus),
 * API Key 通过环境变量 DASHSCOPE_API_KEY 注入。</p>
 *
 * @author sm
 */
@Configuration
public class PerceptionConfig {

    /**
     * 智能体编排决策模型(ReAct 循环,与视觉感知共用同一多模态模型)
     */
    @Bean
    public Model inspectorModel(
        @Value("${inspection.dashscope.api-key:}") String apiKey,
        @Value("${inspection.dashscope.model:qwen3-vl-plus}") String model) {
        return DashScopeChatModel.builder()
            .apiKey(apiKey)
            .modelName(model)
            .stream(false)
            .build();
    }

    /**
     * 多模态视觉感知客户端
     */
    @Bean
    public VisionClient dashScopeVisionClient(
        @Value("${inspection.dashscope.api-key:}") String apiKey,
        @Value("${inspection.dashscope.base-url:https://dashscope.aliyuncs.com/compatible-mode/v1}") String baseUrl,
        @Value("${inspection.dashscope.model:qwen3-vl-plus}") String model) {
        return new DashScopeVisionClient(apiKey, baseUrl, model);
    }
}
