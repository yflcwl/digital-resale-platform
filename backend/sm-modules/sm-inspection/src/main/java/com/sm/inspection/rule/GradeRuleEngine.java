package com.sm.inspection.rule;

import com.sm.inspection.constant.DefectConst;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 成色分级与定价规则引擎(纯函数,可单测)
 *
 * <p>设计原则:模型看、规则判。LLM 只负责感知输出缺陷清单,
 * 成色等级与建议价完全由本引擎按确定性规则裁决,保证结果可复现、可解释。</p>
 *
 * 规则(详见《系统详细设计》3.5):
 * 1. 基准分 100,按缺陷类型×严重度扣分;
 * 2. 同类缺陷扣分合计封顶 30,但 SEVERE 级缺陷不参与封顶;
 * 3. 等级阈值:S ≥ 95、A ≥ 85、B ≥ 70、C ≥ 55、D &lt; 55;
 * 4. 建议价 = 二手行情价中值 × 折损系数,折损系数 = 1 − (100 − 成色分) / 200;
 * 5. 价格区间 [建议价×0.9, 建议价×1.05]。
 *
 * @author sm
 */
public final class GradeRuleEngine {

    /** 同类缺陷扣分封顶 */
    public static final int SAME_TYPE_CAP = 30;
    /** 定价折损最大幅度(分母) */
    private static final BigDecimal LOSS_DENOMINATOR = BigDecimal.valueOf(200);
    /** 价格区间下浮 / 上浮系数 */
    private static final BigDecimal PRICE_MIN_FACTOR = new BigDecimal("0.9");
    private static final BigDecimal PRICE_MAX_FACTOR = new BigDecimal("1.05");

    /** 缺陷权重表:类型 → 严重度 → 扣分 */
    private static final Map<String, Map<String, Integer>> WEIGHTS = new LinkedHashMap<>();

    static {
        WEIGHTS.put(DefectConst.TYPE_SCRATCH, Map.of(
            DefectConst.SEVERITY_MILD, 2,
            DefectConst.SEVERITY_MODERATE, 4,
            DefectConst.SEVERITY_SEVERE, 8));
        WEIGHTS.put(DefectConst.TYPE_DENT, Map.of(
            DefectConst.SEVERITY_MILD, 4,
            DefectConst.SEVERITY_MODERATE, 8,
            DefectConst.SEVERITY_SEVERE, 15));
        WEIGHTS.put(DefectConst.TYPE_PAINT_LOSS, Map.of(
            DefectConst.SEVERITY_MILD, 1,
            DefectConst.SEVERITY_MODERATE, 3,
            DefectConst.SEVERITY_SEVERE, 6));
        WEIGHTS.put(DefectConst.TYPE_CRACK, Map.of(
            DefectConst.SEVERITY_MILD, 6,
            DefectConst.SEVERITY_MODERATE, 12,
            DefectConst.SEVERITY_SEVERE, 20));
        WEIGHTS.put(DefectConst.TYPE_WATER_DAMAGE, Map.of(
            DefectConst.SEVERITY_MILD, 10,
            DefectConst.SEVERITY_MODERATE, 18,
            DefectConst.SEVERITY_SEVERE, 30));
        WEIGHTS.put(DefectConst.TYPE_OTHER, Map.of(
            DefectConst.SEVERITY_MILD, 2,
            DefectConst.SEVERITY_MODERATE, 5,
            DefectConst.SEVERITY_SEVERE, 10));
    }

    private GradeRuleEngine() {
    }

    /**
     * 缺陷条目(与持久化实体解耦,便于纯函数测试)
     */
    public record DefectItem(String type, String position, String severity, Double confidence) {
    }

    /**
     * 单条缺陷的扣分明细
     */
    public record Deduction(String type, String position, String severity, int weight) {
    }

    /**
     * 裁决结果
     */
    public record GradeResult(int score, String grade, int totalDeduction,
                              List<Deduction> details, BigDecimal priceMin, BigDecimal priceMax) {
    }

    /**
     * 仅计算成色分与等级(不含价格)
     */
    public static GradeResult grade(List<DefectItem> defects) {
        return gradeAndPrice(defects, null);
    }

    /**
     * 计算成色分、等级与建议价区间
     *
     * @param defects  缺陷清单
     * @param basePrice 二手行情价中值(可为空,为空时价格区间为 null)
     */
    public static GradeResult gradeAndPrice(List<DefectItem> defects, BigDecimal basePrice) {
        List<Deduction> details = new ArrayList<>();
        // 类型 → 非 SEVERE 扣分合计
        Map<String, Integer> cappedByType = new LinkedHashMap<>();
        int severeTotal = 0;

        if (defects != null) {
            for (DefectItem defect : defects) {
                int weight = weightOf(defect.type(), defect.severity());
                if (weight <= 0) {
                    continue;
                }
                details.add(new Deduction(defect.type(), defect.position(), defect.severity(), weight));
                if (DefectConst.SEVERITY_SEVERE.equals(defect.severity())) {
                    // SEVERE 不参与封顶
                    severeTotal += weight;
                } else {
                    cappedByType.merge(defect.type(), weight, Integer::sum);
                }
            }
        }

        int total = severeTotal;
        for (int sum : cappedByType.values()) {
            total += Math.min(sum, SAME_TYPE_CAP);
        }
        int score = Math.max(0, 100 - total);
        String grade = gradeOf(score);

        BigDecimal priceMin = null;
        BigDecimal priceMax = null;
        if (basePrice != null && basePrice.compareTo(BigDecimal.ZERO) > 0) {
            // 折损系数 = 1 − (100 − score) / 200
            BigDecimal loss = BigDecimal.valueOf(100 - score)
                .divide(LOSS_DENOMINATOR, 4, RoundingMode.HALF_UP);
            BigDecimal factor = BigDecimal.ONE.subtract(loss);
            BigDecimal suggest = basePrice.multiply(factor);
            priceMin = suggest.multiply(PRICE_MIN_FACTOR).setScale(2, RoundingMode.HALF_UP);
            priceMax = suggest.multiply(PRICE_MAX_FACTOR).setScale(2, RoundingMode.HALF_UP);
        }
        return new GradeResult(score, grade, total, details, priceMin, priceMax);
    }

    /**
     * 等级阈值:S ≥ 95、A ≥ 85、B ≥ 70、C ≥ 55、D &lt; 55
     */
    public static String gradeOf(int score) {
        if (score >= 95) {
            return "S";
        }
        if (score >= 85) {
            return "A";
        }
        if (score >= 70) {
            return "B";
        }
        if (score >= 55) {
            return "C";
        }
        return "D";
    }

    /**
     * 查询缺陷权重,未知类型/严重度按 0 处理
     */
    public static int weightOf(String type, String severity) {
        Map<String, Integer> bySeverity = WEIGHTS.get(type);
        if (bySeverity == null) {
            return 0;
        }
        Integer weight = bySeverity.get(severity);
        return weight == null ? 0 : weight;
    }
}
