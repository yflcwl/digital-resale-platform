package com.sm.inspection.rule;

import com.sm.inspection.constant.DefectConst;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 规则引擎单元测试(纯函数,无外部依赖)
 *
 * 覆盖:权重表、同类封顶、SEVERE 不封顶、等级阈值边界、定价公式与区间。
 */
@Tag("dev")
class GradeRuleEngineTest {

    private GradeRuleEngine.DefectItem defect(String type, String severity) {
        return new GradeRuleEngine.DefectItem(type, "屏幕", severity, 0.9);
    }

    @Test
    void emptyDefectsIsS() {
        GradeRuleEngine.GradeResult result = GradeRuleEngine.grade(List.of());
        assertEquals(100, result.score());
        assertEquals("S", result.grade());
        assertEquals(0, result.totalDeduction());
    }

    @Test
    void exampleFromDesignDoc() {
        // 详细设计示例:划痕轻微(2) + 边框磕碰中等(8)= 90 分 → A 级
        GradeRuleEngine.GradeResult result = GradeRuleEngine.gradeAndPrice(List.of(
            defect(DefectConst.TYPE_SCRATCH, DefectConst.SEVERITY_MILD),
            defect(DefectConst.TYPE_DENT, DefectConst.SEVERITY_MODERATE)),
            new BigDecimal("3800"));
        assertEquals(10, result.totalDeduction());
        assertEquals(90, result.score());
        assertEquals("A", result.grade());
        // 建议价 = 3800 × (1 − 10/200) = 3610,区间 [3249, 3790.5]
        assertEquals(new BigDecimal("3249.00"), result.priceMin());
        assertEquals(new BigDecimal("3790.50"), result.priceMax());
    }

    @Test
    void sameTypeCapAt30() {
        // 同类型 MILD 划痕 20 处 = 40 分,封顶 30 → 70 分 B 级
        List<GradeRuleEngine.DefectItem> defects = java.util.stream.IntStream.range(0, 20)
            .mapToObj(i -> defect(DefectConst.TYPE_SCRATCH, DefectConst.SEVERITY_MILD))
            .toList();
        GradeRuleEngine.GradeResult result = GradeRuleEngine.grade(defects);
        assertEquals(30, result.totalDeduction());
        assertEquals(70, result.score());
        assertEquals("B", result.grade());
    }

    @Test
    void severeNotCapped() {
        // 3 处 SEVERE 裂纹 = 60 分,不参与封顶 → 40 分 D 级(而非被封顶成 70 分)
        List<GradeRuleEngine.DefectItem> defects = List.of(
            defect(DefectConst.TYPE_CRACK, DefectConst.SEVERITY_SEVERE),
            defect(DefectConst.TYPE_CRACK, DefectConst.SEVERITY_SEVERE),
            defect(DefectConst.TYPE_CRACK, DefectConst.SEVERITY_SEVERE));
        GradeRuleEngine.GradeResult result = GradeRuleEngine.grade(defects);
        assertEquals(60, result.totalDeduction());
        assertEquals(40, result.score());
        assertEquals("D", result.grade());
    }

    @Test
    void mixedCapOnlyAffectsNonSevere() {
        // 同类型:MILD 4×4=16(封顶内)+ SEVERE 1×15(不封顶)→ 31 分扣分 → 69 分(55≤69<70 → C 级)
        GradeRuleEngine.GradeResult result = GradeRuleEngine.grade(List.of(
            defect(DefectConst.TYPE_DENT, DefectConst.SEVERITY_MILD),
            defect(DefectConst.TYPE_DENT, DefectConst.SEVERITY_MILD),
            defect(DefectConst.TYPE_DENT, DefectConst.SEVERITY_MILD),
            defect(DefectConst.TYPE_DENT, DefectConst.SEVERITY_MILD),
            defect(DefectConst.TYPE_DENT, DefectConst.SEVERITY_SEVERE)));
        assertEquals(16 + 15, result.totalDeduction());
        assertEquals(69, result.score());
        assertEquals("C", result.grade());
    }

    @Test
    void gradeThresholds() {
        assertEquals("S", GradeRuleEngine.gradeOf(95));
        assertEquals("A", GradeRuleEngine.gradeOf(94));
        assertEquals("A", GradeRuleEngine.gradeOf(85));
        assertEquals("B", GradeRuleEngine.gradeOf(84));
        assertEquals("B", GradeRuleEngine.gradeOf(70));
        assertEquals("C", GradeRuleEngine.gradeOf(69));
        assertEquals("C", GradeRuleEngine.gradeOf(55));
        assertEquals("D", GradeRuleEngine.gradeOf(54));
        assertEquals("D", GradeRuleEngine.gradeOf(0));
    }

    @Test
    void scoreFloorAtZero() {
        // 扣分超过 100 时分数为 0
        GradeRuleEngine.GradeResult result = GradeRuleEngine.grade(List.of(
            defect(DefectConst.TYPE_WATER_DAMAGE, DefectConst.SEVERITY_SEVERE),
            defect(DefectConst.TYPE_WATER_DAMAGE, DefectConst.SEVERITY_SEVERE),
            defect(DefectConst.TYPE_WATER_DAMAGE, DefectConst.SEVERITY_SEVERE),
            defect(DefectConst.TYPE_WATER_DAMAGE, DefectConst.SEVERITY_SEVERE)));
        assertEquals(0, result.score());
        assertEquals("D", result.grade());
    }

    @Test
    void priceWithoutBaseIsNull() {
        GradeRuleEngine.GradeResult result = GradeRuleEngine.grade(List.of(
            defect(DefectConst.TYPE_SCRATCH, DefectConst.SEVERITY_MILD)));
        assertNull(result.priceMin());
        assertNull(result.priceMax());
    }

    @Test
    void unknownTypeIgnored() {
        GradeRuleEngine.DefectItem unknown = new GradeRuleEngine.DefectItem("UNKNOWN", "屏幕", "MILD", 0.5);
        GradeRuleEngine.GradeResult result = GradeRuleEngine.grade(List.of(unknown));
        assertEquals(0, result.totalDeduction());
        assertEquals(100, result.score());
    }

    @Test
    void weightTableMatchesDesign() {
        assertEquals(2, GradeRuleEngine.weightOf(DefectConst.TYPE_SCRATCH, DefectConst.SEVERITY_MILD));
        assertEquals(4, GradeRuleEngine.weightOf(DefectConst.TYPE_SCRATCH, DefectConst.SEVERITY_MODERATE));
        assertEquals(8, GradeRuleEngine.weightOf(DefectConst.TYPE_SCRATCH, DefectConst.SEVERITY_SEVERE));
        assertEquals(30, GradeRuleEngine.weightOf(DefectConst.TYPE_WATER_DAMAGE, DefectConst.SEVERITY_SEVERE));
        assertTrue(GradeRuleEngine.SAME_TYPE_CAP == 30);
    }
}
