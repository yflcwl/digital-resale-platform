package com.sm.inspection.config;

import com.sm.inspection.perception.DashScopeVisionClient;
import com.sm.inspection.perception.VisionClient;
import io.agentscope.core.model.Model;
import io.agentscope.extensions.model.dashscope.DashScopeChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 质检感知层装配:接入通义千问(DashScope)
 *
 * <p>视觉感知使用 qwen-vl-max 多模态模型,智能体编排使用 qwen-plus,
 * API Key 通过环境变量 DASHSCOPE_API_KEY 注入。</p>
 *
 * @author sm
 */
@Configuration
public class PerceptionConfig {

    /**
     * 智能体编排决策模型(ReAct 循环)
     */
    @Bean
    public Model inspectorModel(
        @Value("${inspection.dashscope.api-key:}") String apiKey,
        @Value("${inspection.dashscope.agent-model:qwen-plus}") String agentModel) {
        return DashScopeChatModel.builder()
            .apiKey(apiKey)
            .modelName(agentModel)
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
        @Value("${inspection.dashscope.model:qwen-vl-max}") String model) {
        return new DashScopeVisionClient(apiKey, baseUrl, model);
    }
}
