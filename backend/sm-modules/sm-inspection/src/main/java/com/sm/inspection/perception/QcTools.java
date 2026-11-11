package com.sm.inspection.perception;

import com.sm.common.json.utils.JsonUtils;
import com.sm.inspection.rule.GradeRuleEngine;
import io.agentscope.core.tool.Tool;
import io.agentscope.core.tool.ToolParam;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 质检智能体工具集(AgentScope Tool)
 *
 * <p>由 ReAct 智能体在推理循环中自主调用。感知工具内部调用多模态模型"看",
 * 分级定价工具内部调用规则引擎"判"——工具只提供能力,是否/何时调用由模型决策。</p>
 *
 * <p>每个任务构造一个实例(绑定商品与照片),执行结束后由服务读取 {@code result()} 落库。</p>
 *
 * @author sm
 */
@Getter
public class QcTools {

    private final VisionClient visionClient;
    private final Long productId;
    private final List<String> photos;

    /* ---------- 执行结果(供服务层读取) ---------- */
    private VisionClient.ComplianceResult compliance;
    private VisionClient.ModelIdentifyResult modelInfo;
    private List<GradeRuleEngine.DefectItem> defects;
    private GradeRuleEngine.GradeResult grade;

    public QcTools(VisionClient visionClient, Long productId, List<String> photos) {
        this.visionClient = visionClient;
        this.productId = productId;
        this.photos = photos;
    }

    @Tool(name = "check_compliance",
        description = "对当前商品照片做拍照合规校验:核对角度是否齐全(正面/背面/侧面/屏幕点亮/序列号)、清晰度是否达标。返回 JSON,passed=false 时需提示用户补拍。")
    public String checkCompliance() {
        compliance = visionClient.checkCompliance(productId, photos);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("passed", compliance.passed());
        result.put("missing", compliance.missingAngles());
        result.put("clarity", compliance.clarity());
        result.put("suggestion", compliance.suggestion());
        return JsonUtils.toJsonString(result);
    }

    @Tool(name = "recognize_model",
        description = "识别商品品牌、型号与配置,并给出该机型二手行情价区间。返回 JSON。")
    public String recognizeModel() {
        modelInfo = visionClient.recognizeModel(productId, photos);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("brand", modelInfo.brand());
        result.put("model", modelInfo.model());
        result.put("priceMin", modelInfo.marketPriceMin());
        result.put("priceMax", modelInfo.marketPriceMax());
        result.put("confidence", modelInfo.confidence());
        return JsonUtils.toJsonString(result);
    }

    @Tool(name = "analyze_defects",
        description = "对外观照片做缺陷检测,逐项输出缺陷类型(SCRATCH/CRACK/DENT/PAINT_LOSS/WATER_DAMAGE/OTHER)、位置、严重度(MILD/MODERATE/SEVERE)与置信度。返回 JSON。")
    public String analyzeDefects() {
        defects = visionClient.analyzeDefects(productId, photos);
        List<Map<String, Object>> list = new ArrayList<>();
        if (defects != null) {
            for (GradeRuleEngine.DefectItem defect : defects) {
                Map<String, Object> item = new LinkedHashMap<>();
                item.put("type", defect.type());
                item.put("position", defect.position());
                item.put("severity", defect.severity());
                item.put("confidence", defect.confidence());
                list.add(item);
            }
        }
        return JsonUtils.toJsonString(Map.of("defects", list));
    }

    @Tool(name = "grade_and_price",
        description = "按系统规则引擎(缺陷权重表)计算成色分、成色等级(S/A/B/C/D)与参考价格区间。必须传入缺陷清单 defects_json,分级与定价只能由该工具裁决,不得自行推断。")
    public String gradeAndPrice(
        @ToolParam(name = "defects_json", description = "缺陷清单 JSON 数组,元素含 type/position/severity/confidence")
            String defectsJson) {
        List<GradeRuleEngine.DefectItem> items = parseDefects(defectsJson);
        if (items.isEmpty() && defects != null) {
            items = defects;
        }
        BigDecimal basePrice = null;
        if (modelInfo != null && modelInfo.marketPriceMin() != null && modelInfo.marketPriceMax() != null) {
            basePrice = modelInfo.marketPriceMin().add(modelInfo.marketPriceMax())
                .divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);
        }
        grade = GradeRuleEngine.gradeAndPrice(items, basePrice);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("score", grade.score());
        result.put("grade", grade.grade());
        result.put("deduction", grade.totalDeduction());
        result.put("priceMin", grade.priceMin());
        result.put("priceMax", grade.priceMax());
        result.put("details", grade.details());
        return JsonUtils.toJsonString(result);
    }

    /**
     * 容错解析:支持 JSON 数组或 {"defects":[...]} 两种形态
     */
    private List<GradeRuleEngine.DefectItem> parseDefects(String defectsJson) {
        List<GradeRuleEngine.DefectItem> items = new ArrayList<>();
        if (defectsJson == null || defectsJson.isBlank()) {
            return items;
        }
        String json = defectsJson.trim();
        int arrayStart = json.indexOf('[');
        int arrayEnd = json.lastIndexOf(']');
        if (arrayStart < 0 || arrayEnd <= arrayStart) {
            return items;
        }
        String arrayJson = json.substring(arrayStart, arrayEnd + 1);
        try {
            List<?> rawList = JsonUtils.parseArray(arrayJson, Map.class);
            for (Object item : rawList) {
                if (!(item instanceof Map<?, ?> raw)) {
                    continue;
                }
                String type = raw.get("type") == null ? null : String.valueOf(raw.get("type"));
                String severity = raw.get("severity") == null ? null : String.valueOf(raw.get("severity"));
                if (type == null || severity == null) {
                    continue;
                }
                Double confidence = raw.get("confidence") == null ? 0.8
                    : Double.parseDouble(String.valueOf(raw.get("confidence")));
                items.add(new GradeRuleEngine.DefectItem(type,
                    raw.get("position") == null ? "" : String.valueOf(raw.get("position")),
                    severity, confidence));
            }
        } catch (Exception ignored) {
            // 解析失败时回退到缓存的缺陷结果
        }
        return items;
    }
}
