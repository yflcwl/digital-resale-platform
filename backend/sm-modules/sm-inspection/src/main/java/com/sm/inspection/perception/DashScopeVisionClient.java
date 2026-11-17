package com.sm.inspection.perception;

import com.sm.common.core.utils.StringUtils;
import com.sm.common.json.utils.JsonUtils;
import com.sm.inspection.rule.GradeRuleEngine;
import io.agentscope.core.util.JsonSchemaUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * DashScope(通义千问)多模态感知实现(OpenAI 兼容模式)
 *
 * <p>输出格式在调用处通过 response_format(json_schema)约束:JSON Schema 由
 * {@link QcOutputs} 的 POJO 生成,响应直接反序列化为 POJO,不依赖提示词中的 JSON 示例。
 * 提示词从 classpath:prompts/ 加载。</p>
 *
 * @author sm
 */
@Slf4j
public class DashScopeVisionClient implements VisionClient {

    private final RestClient restClient;
    private final String model;

    public DashScopeVisionClient(@Value("${inspection.dashscope.api-key:}") String apiKey,
                                 @Value("${inspection.dashscope.base-url:https://dashscope.aliyuncs.com/compatible-mode/v1}") String baseUrl,
                                 @Value("${inspection.dashscope.model:qwen3-vl-plus}") String model) {
        this.model = model;
        this.restClient = RestClient.builder()
            .baseUrl(baseUrl)
            .defaultHeader("Authorization", "Bearer " + apiKey)
            .build();
    }

    @Override
    public String modelName() {
        return model;
    }

    @Override
    public ComplianceResult checkCompliance(Long productId, List<String> photos) {
        QcOutputs.Compliance out = callWithImages(
            PromptLoader.load("vision-compliance"), photos, QcOutputs.Compliance.class);
        return new ComplianceResult(out.passed,
            out.missing == null ? List.of() : out.missing,
            out.clarity,
            out.suggestion == null ? "" : out.suggestion);
    }

    @Override
    public ModelIdentifyResult recognizeModel(Long productId, List<String> photos) {
        QcOutputs.ModelIdentify out = callWithImages(
            PromptLoader.load("vision-recognize"), photos, QcOutputs.ModelIdentify.class);
        return new ModelIdentifyResult(
            out.brand == null ? "未知品牌" : out.brand,
            out.model == null ? "未知机型" : out.model,
            out.priceMin, out.priceMax, out.confidence);
    }

    @Override
    public List<GradeRuleEngine.DefectItem> analyzeDefects(Long productId, List<String> photos) {
        QcOutputs.Defects out = callWithImages(
            PromptLoader.load("vision-defects"), photos, QcOutputs.Defects.class);
        List<GradeRuleEngine.DefectItem> result = new ArrayList<>();
        if (out.defects != null) {
            for (QcOutputs.Defect defect : out.defects) {
                if (StringUtils.isBlank(defect.type) || StringUtils.isBlank(defect.severity)) {
                    continue;
                }
                result.add(new GradeRuleEngine.DefectItem(defect.type,
                    defect.position == null ? "" : defect.position,
                    defect.severity, defect.confidence));
            }
        }
        return result;
    }

    /**
     * 多图视觉对话:调用处用 POJO 生成的 JSON Schema 约束输出格式,并反序列化为该 POJO
     */
    private <T> T callWithImages(String prompt, List<String> photos, Class<T> outputType) {
        List<Map<String, Object>> content = new ArrayList<>();
        if (photos != null) {
            for (String url : photos) {
                content.add(Map.of("type", "image_url", "image_url", Map.of("url", url)));
            }
        }
        content.add(Map.of("type", "text", "text", prompt));

        Map<String, Object> body = Map.of(
            "model", model,
            "messages", List.of(Map.of("role", "user", "content", content)),
            "temperature", 0.1,
            "response_format", Map.of(
                "type", "json_schema",
                "json_schema", Map.of(
                    "name", outputType.getSimpleName(),
                    "schema", JsonSchemaUtils.generateSchemaFromClass(outputType))));

        String resp = restClient.post()
            .uri("/chat/completions")
            .contentType(MediaType.APPLICATION_JSON)
            .body(body)
            .retrieve()
            .body(String.class);

        if (StringUtils.isBlank(resp)) {
            throw new IllegalStateException("多模态模型返回为空");
        }
        return JsonUtils.parseObject(extractJson(extractMessageText(resp)), outputType);
    }

    /**
     * 提取模型回复正文(兼容 markdown 代码块中的 JSON)
     */
    @SuppressWarnings("unchecked")
    private String extractMessageText(String resp) {
        Map<String, Object> root = JsonUtils.parseMap(resp);
        List<Map<String, Object>> choices = (List<Map<String, Object>>) root.get("choices");
        if (choices == null || choices.isEmpty()) {
            throw new IllegalStateException("多模态模型返回无 choices");
        }
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        String text = message == null ? null : String.valueOf(message.get("content"));
        if (StringUtils.isBlank(text)) {
            throw new IllegalStateException("模型输出为空");
        }
        return text;
    }

    /**
     * 从模型输出中提取 JSON 对象(容忍 markdown 代码块包裹)
     */
    private String extractJson(String text) {
        String json = text.trim();
        int start = json.indexOf('{');
        int end = json.lastIndexOf('}');
        if (start < 0 || end <= start) {
            throw new IllegalStateException("模型输出不含 JSON: " + text);
        }
        return json.substring(start, end + 1);
    }
}
