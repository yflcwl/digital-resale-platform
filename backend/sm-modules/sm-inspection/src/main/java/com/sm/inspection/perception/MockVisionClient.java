package com.sm.inspection.perception;

import com.sm.inspection.constant.DefectConst;
import com.sm.inspection.rule.GradeRuleEngine;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.List;

/**
 * Mock 感知实现:无多模态 API Key 时的演示模式
 *
 * <p>返回稳定可预期的感知结果,用于本地演示与集成测试;
 * 规则裁决仍走真实规则引擎,保证"模型看、规则判"链路完整。</p>
 *
 * @author sm
 */
@Slf4j
public class MockVisionClient implements VisionClient {

    @Override
    public String modelName() {
        return "mock-vision";
    }

    @Override
    public ComplianceResult checkCompliance(Long productId, List<String> photos) {
        int size = photos == null ? 0 : photos.size();
        if (size < 3) {
            return new ComplianceResult(false,
                List.of("屏幕点亮照", "序列号标签"),
                0.86,
                "照片数量不足,请补充屏幕点亮与序列号标签照片后重试");
        }
        return new ComplianceResult(true, List.of(), 0.93, "");
    }

    @Override
    public ModelIdentifyResult recognizeModel(Long productId, List<String> photos) {
        return new ModelIdentifyResult("Apple", "iPhone 13 128G(演示识别)",
            new BigDecimal("3500"), new BigDecimal("3900"), 0.92);
    }

    @Override
    public List<GradeRuleEngine.DefectItem> analyzeDefects(Long productId, List<String> photos) {
        return List.of(
            new GradeRuleEngine.DefectItem(DefectConst.TYPE_SCRATCH, "屏幕", DefectConst.SEVERITY_MILD, 0.92),
            new GradeRuleEngine.DefectItem(DefectConst.TYPE_DENT, "边框", DefectConst.SEVERITY_MODERATE, 0.85));
    }
}
