package com.sm.inspection.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sm.common.mybatis.core.domain.BaseEntity;
import org.apache.ibatis.type.JdbcType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * 质检任务对象 sm_inspection_task
 *
 * @author sm
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sm_inspection_task", autoResultMap = true)
public class InspectionTask extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(value = "id")
    private Long id;

    /** 商品ID */
    private Long productId;

    /** 质检轮次(从1开始) */
    private Integer roundNo;

    /** 任务状态 */
    private String status;

    /** 提交照片URL列表(jsonb) */
    @TableField(typeHandler = com.sm.inspection.handler.JsonbStringTypeHandler.class, jdbcType = JdbcType.OTHER)
    private String photoUrls;

    /** RocketMQ消息ID */
    private String mqMsgId;

    /** 使用的多模态模型标识 */
    private String modelName;

    /** 综合置信度(0~100) */
    private BigDecimal confidence;

    /** 质检耗时(毫秒) */
    private Long durationMs;

    /** 失败原因 */
    private String errorMsg;

    /** 删除标志(0正常 2删除) */
    @TableLogic
    private String delFlag;
}
