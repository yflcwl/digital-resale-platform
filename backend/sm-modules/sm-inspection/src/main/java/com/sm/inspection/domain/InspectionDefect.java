package com.sm.inspection.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 质检缺陷明细对象 sm_inspection_defect
 *
 * @author sm
 */
@Data
@TableName("sm_inspection_defect")
public class InspectionDefect implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id")
    private Long id;

    /** 报告ID */
    private Long reportId;

    /** 缺陷类型 */
    private String defectType;

    /** 位置 */
    private String position;

    /** 严重程度 */
    private String severity;

    /** 描述 */
    private String description;

    /** 缺陷截图 */
    private String imageUrl;

    /** 排序 */
    private Integer sort;
}
