package com.sm.inspection.config;

import com.sm.inspection.perception.DashScopeVisionClient;
import com.sm.inspection.perception.MockVisionClient;
import com.sm.inspection.perception.VisionClient;
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

    @Bean
    @ConditionalOnProperty(name = "inspection.mock", havingValue = "false")
    public VisionClient dashScopeVisionClient(
        @Value("${inspection.dashscope.api-key:}") String apiKey,
        @Value("${inspection.dashscope.base-url:https://dashscope.aliyuncs.com/compatible-mode/v1}") String baseUrl,
        @Value("${inspection.dashscope.model:qwen-vl-max}") String model) {
        return new DashScopeVisionClient(apiKey, baseUrl, model);
    }
}
