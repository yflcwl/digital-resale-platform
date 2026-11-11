package com.sm.inspection.config;

import com.sm.inspection.perception.DashScopeVisionClient;
import com.sm.inspection.perception.MockVisionClient;
import com.sm.inspection.perception.ScriptedReActModel;
import com.sm.inspection.perception.VisionClient;
import io.agentscope.core.model.Model;
import io.agentscope.extensions.model.dashscope.DashScopeChatModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 质检感知层装配
 *
 * <p>inspection.mock=true(默认)使用 Mock 感知,便于无 API Key 演示;
 * 设为 false 且配置 inspection.dashscope.api-key 后接入通义千问 VL。</p>
 *
 * @author sm
 */
@Configuration
public class PerceptionConfig {

    @Bean
    @ConditionalOnProperty(name = "inspection.mock", havingValue = "true", matchIfMissing = true)
    public VisionClient mockVisionClient() {
        return new MockVisionClient();
    }

    /**
     * 智能体决策模型:
     * mock 模式使用离线 ReAct 模型(真实运行 Thought→Action→Observation 循环),
     * 非 mock 模式接入通义千问(DashScope)进行编排决策。
     */
    @Bean
    @ConditionalOnProperty(name = "inspection.mock", havingValue = "true", matchIfMissing = true)
    public Model scriptedReActModel() {
        return new ScriptedReActModel();
    }

    @Bean
    @ConditionalOnProperty(name = "inspection.mock", havingValue = "false")
    public Model dashScopeReActModel(
        @Value("${inspection.dashscope.api-key:}") String apiKey,
        @Value("${inspection.dashscope.agent-model:qwen-plus}") String agentModel) {
        return DashScopeChatModel.builder()
            .apiKey(apiKey)
            .modelName(agentModel)
            .stream(false)
            .build();
    }

    @Bean
    @ConditionalOnProperty(name = "inspection.mock", havingValue = "false")
    public VisionClient dashScopeVisionClient(
        @Value("${inspection.dashscope.api-key:}") String apiKey,
        @Value("${inspection.dashscope.base-url:https://dashscope.aliyuncs.com/compatible-mode/v1}") String baseUrl,
        @Value("${inspection.dashscope.model:qwen-vl-max}") String model) {
        return new DashScopeVisionClient(apiKey, baseUrl, model);
    }
}
