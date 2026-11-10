package com.sm.inspection.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 质检报告反馈对象 sm_inspection_feedback
 *
 * @author sm
 */
@Data
@TableName("sm_inspection_feedback")
public class InspectionFeedback implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id")
    private Long id;

    /** 报告ID */
    private Long reportId;

    /** 反馈用户ID */
    private Long userId;

    /** 反馈内容 */
    private String content;

    /** 处理状态 */
    private String status;

    /** 处理回复 */
    private String reply;

    /** 回复时间 */
    private Date replyTime;

    /** 反馈时间 */
    private Date createTime;
}
