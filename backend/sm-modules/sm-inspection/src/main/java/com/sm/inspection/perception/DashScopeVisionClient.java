package com.sm.inspection.perception;

import com.sm.common.core.utils.StringUtils;
import com.sm.common.json.utils.JsonUtils;
import com.sm.inspection.constant.DefectConst;
import com.sm.inspection.rule.GradeRuleEngine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * DashScope(通义千问 VL)多模态感知实现(OpenAI 兼容模式)
 *
 * <p>需要配置 inspection.dashscope.api-key(环境变量 DASHSCOPE_API_KEY)。
 * 模型输出统一约束为 JSON,解析失败按工具失败处理(由上层重试/降级)。</p>
 *
 * @author sm
 */
@Slf4j
public class DashScopeVisionClient implements VisionClient {

    private final RestClient restClient;
    private final String model;

    public DashScopeVisionClient(@Value("${inspection.dashscope.api-key:}") String apiKey,
                                 @Value("${inspection.dashscope.base-url:https://dashscope.aliyuncs.com/compatible-mode/v1}") String baseUrl,
                                 @Value("${inspection.dashscope.model:qwen-vl-max}") String model) {
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
        String prompt = "你是二手数码质检员。请核对照片是否覆盖:正面、背面、侧面、屏幕点亮、序列号标签,"
            + "并评估清晰度(0~1)。仅输出 JSON:"
            + "{\"passed\":true,\"missing\":[],\"clarity\":0.9,\"suggestion\":\"\"}";
        Map<String, Object> json = callWithImages(prompt, photos);
        boolean passed = Boolean.TRUE.equals(json.get("passed"));
        List<String> missing = new ArrayList<>();
        Object missingObj = json.get("missing");
        if (missingObj instanceof List<?> list) {
            list.forEach(m -> missing.add(String.valueOf(m)));
        }
        double clarity = toDouble(json.get("clarity"), 0.9);
        return new ComplianceResult(passed, missing, clarity,
            json.get("suggestion") == null ? "" : String.valueOf(json.get("suggestion")));
    }

    @Override
    public ModelIdentifyResult recognizeModel(Long productId, List<String> photos) {
        String prompt = "你是机型鉴定专家。结合外观与标签文字识别品牌、型号、配置,并给出二手行情价区间。"
            + "仅输出 JSON:{\"brand\":\"Apple\",\"model\":\"iPhone 13 128G\",\"priceMin\":3500,\"priceMax\":3900,\"confidence\":0.92}";
        Map<String, Object> json = callWithImages(prompt, photos);
        BigDecimal min = toDecimal(json.get("priceMin"));
        BigDecimal max = toDecimal(json.get("priceMax"));
        return new ModelIdentifyResult(
            json.get("brand") == null ? "未知品牌" : String.valueOf(json.get("brand")),
            json.get("model") == null ? "未知机型" : String.valueOf(json.get("model")),
            min, max, toDouble(json.get("confidence"), 0.7));
    }

    @Override
    public List<GradeRuleEngine.DefectItem> analyzeDefects(Long productId, List<String> photos) {
        String prompt = "你是二手数码外观质检员。逐项检测缺陷,类型仅限:SCRATCH/CRACK/DENT/PAINT_LOSS/WATER_DAMAGE/OTHER,"
            + "严重度仅限:MILD/MODERATE/SEVERE。未发现即不输出。仅输出 JSON:"
            + "{\"defects\":[{\"type\":\"SCRATCH\",\"position\":\"屏幕\",\"severity\":\"MILD\",\"confidence\":0.9}]}";
        Map<String, Object> json = callWithImages(prompt, photos);
        List<GradeRuleEngine.DefectItem> result = new ArrayList<>();
        Object defectsObj = json.get("defects");
        if (defectsObj instanceof List<?> list) {
            for (Object item : list) {
                if (item instanceof Map<?, ?> map) {
                    String type = String.valueOf(map.get("type"));
                    String severity = String.valueOf(map.get("severity"));
                    if (StringUtils.isBlank(type) || StringUtils.isBlank(severity)) {
                        continue;
                    }
                    result.add(new GradeRuleEngine.DefectItem(type,
                        map.get("position") == null ? "" : String.valueOf(map.get("position")),
                        severity, toDouble(map.get("confidence"), 0.8)));
                }
            }
        }
        return result;
    }

    /**
     * 多图视觉对话,输出 JSON
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> callWithImages(String prompt, List<String> photos) {
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
            "temperature", 0.1);

        String resp = restClient.post()
            .uri("/chat/completions")
            .contentType(MediaType.APPLICATION_JSON)
            .body(body)
            .retrieve()
            .body(String.class);

        if (StringUtils.isBlank(resp)) {
            throw new IllegalStateException("多模态模型返回为空");
        }
        Map<String, Object> root = JsonUtils.parseMap(resp);
        List<Map<String, Object>> choices = (List<Map<String, Object>>) root.get("choices");
        if (choices == null || choices.isEmpty()) {
            throw new IllegalStateException("多模态模型返回无 choices");
        }
        Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
        String text = message == null ? null : String.valueOf(message.get("content"));
        return parseJsonBlock(text);
    }

    /**
     * 从模型输出中提取 JSON(容忍 markdown 代码块包裹)
     */
    private Map<String, Object> parseJsonBlock(String text) {
        if (StringUtils.isBlank(text)) {
            throw new IllegalStateException("模型输出为空");
        }
        String json = text.trim();
        int start = json.indexOf('{');
        int end = json.lastIndexOf('}');
        if (start < 0 || end <= start) {
            throw new IllegalStateException("模型输出不含 JSON: " + text);
        }
        json = json.substring(start, end + 1);
        try {
            return JsonUtils.parseMap(json);
        } catch (Exception e) {
            throw new IllegalStateException("模型输出 JSON 解析失败: " + json, e);
        }
    }

    private double toDouble(Object v, double def) {
        if (v == null) {
            return def;
        }
        try {
            return Double.parseDouble(String.valueOf(v));
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private BigDecimal toDecimal(Object v) {
        if (v == null) {
            return null;
        }
        try {
            return new BigDecimal(String.valueOf(v));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
