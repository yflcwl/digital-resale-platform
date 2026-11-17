package com.sm.inspection.perception;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.math.BigDecimal;
import java.util.List;

/**
 * 质检结构化输出模型
 *
 * <p>由 AgentScope 的 JsonSchemaUtils 根据这些 POJO 生成 JSON Schema,
 * 在模型调用处通过 response_format 约束输出,并作为响应反序列化目标。
 * Agent 最终结论通过 {@code Msg#getStructuredData(Class)} 读取框架校验后的结果。</p>
 *
 * @author sm
 */
public final class QcOutputs {

    private QcOutputs() {
    }

    /**
     * 拍照合规校验结果
     */
    public static class Compliance {
        @JsonPropertyDescription("照片是否满足质检要求")
        public boolean passed;
        @JsonPropertyDescription("缺失的拍摄角度")
        public List<String> missing;
        @JsonPropertyDescription("照片清晰度 0~1")
        public double clarity;
        @JsonPropertyDescription("补拍建议")
        public String suggestion;
    }

    /**
     * 机型识别结果
     */
    public static class ModelIdentify {
        @JsonPropertyDescription("品牌")
        public String brand;
        @JsonPropertyDescription("型号与配置")
        public String model;
        @JsonPropertyDescription("二手行情价下限")
        public BigDecimal priceMin;
        @JsonPropertyDescription("二手行情价上限")
        public BigDecimal priceMax;
        @JsonPropertyDescription("识别置信度 0~1")
        public double confidence;
    }

    /**
     * 外观缺陷检测结果
     */
    public static class Defects {
        @JsonPropertyDescription("缺陷清单,未发现则为空数组")
        public List<Defect> defects;
    }

    public static class Defect {
        @JsonPropertyDescription("缺陷类型 SCRATCH/CRACK/DENT/PAINT_LOSS/WATER_DAMAGE/OTHER")
        public String type;
        @JsonPropertyDescription("缺陷位置")
        public String position;
        @JsonPropertyDescription("严重度 MILD/MODERATE/SEVERE")
        public String severity;
        @JsonPropertyDescription("置信度 0~1")
        public double confidence;
    }

    /**
     * 智能体最终质检结论
     */
    public static class Conclusion {
        @JsonPropertyDescription("成色等级 S/A/B/C/D")
        public String grade;
        @JsonPropertyDescription("一句话质检结论")
        public String summary;
    }
}
