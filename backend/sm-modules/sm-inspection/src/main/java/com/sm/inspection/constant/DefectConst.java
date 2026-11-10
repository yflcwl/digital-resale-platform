package com.sm.inspection.constant;

/**
 * 缺陷类型与严重程度常量
 *
 * @author sm
 */
public interface DefectConst {

    String TYPE_SCRATCH = "SCRATCH";       // 划痕
    String TYPE_CRACK = "CRACK";           // 裂纹
    String TYPE_DENT = "DENT";             // 磕碰
    String TYPE_PAINT_LOSS = "PAINT_LOSS"; // 掉漆
    String TYPE_WATER_DAMAGE = "WATER_DAMAGE"; // 进水痕
    String TYPE_OTHER = "OTHER";           // 其他

    String SEVERITY_MILD = "MILD";         // 轻微
    String SEVERITY_MODERATE = "MODERATE"; // 中等
    String SEVERITY_SEVERE = "SEVERE";     // 严重
}
