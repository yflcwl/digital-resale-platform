package com.sm.inspection.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 质检报告对象 sm_inspection_report
 *
 * @author sm
 */
@Data
@TableName("sm_inspection_report")
public class InspectionReport implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id")
    private Long id;

    /** 质检任务ID */
    private Long taskId;

    /** 商品ID */
    private Long productId;

    /** 成色等级(S/A/B/C/D) */
    private String qualityGrade;

    /** 识别机型 */
    private String brandModel;

    /** 缺陷总结 */
    private String defectSummary;

    /** 建议价格下限 */
    private BigDecimal priceMin;

    /** 建议价格上限 */
    private BigDecimal priceMax;

    /** 置信度 */
    private BigDecimal confidence;

    /** 免责声明 */
    private String disclaimer;

    /** 报告状态(VALID/INVALID) */
    private String status;

    /** 删除标志 */
    @TableLogic
    private String delFlag;

    /** 创建人 */
    private Long createBy;

    /** 创建时间 */
    private Date createTime;

    /** 更新人 */
    private Long updateBy;

    /** 更新时间 */
    private Date updateTime;
}
