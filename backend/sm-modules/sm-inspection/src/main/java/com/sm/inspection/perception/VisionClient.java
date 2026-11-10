package com.sm.inspection.perception;

import com.sm.inspection.rule.GradeRuleEngine;

import java.math.BigDecimal;
import java.util.List;

/**
 * 多模态视觉感知客户端(AgentScope 感知工具的实现层)
 *
 * <p>职责边界:仅负责"看"——对照片做角度合规判断、机型识别、缺陷感知,
 * 不做成色分级与定价(由 {@link GradeRuleEngine} 裁决)。</p>
 *
 * @author sm
 */
public interface VisionClient {

    /**
     * 使用的模型标识
     */
    String modelName();

    /**
     * 拍照合规校验
     */
    ComplianceResult checkCompliance(Long productId, List<String> photos);

    /**
     * 机型识别
     */
    ModelIdentifyResult recognizeModel(Long productId, List<String> photos);

    /**
     * 外观缺陷分析
     */
    List<GradeRuleEngine.DefectItem> analyzeDefects(Long productId, List<String> photos);

    record ComplianceResult(boolean passed, List<String> missingAngles, double clarity, String suggestion) {
    }

    record ModelIdentifyResult(String brand, String model, BigDecimal marketPriceMin,
                               BigDecimal marketPriceMax, double confidence) {
    }
}
