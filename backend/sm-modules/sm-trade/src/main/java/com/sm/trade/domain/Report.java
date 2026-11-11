package com.sm.trade.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 商品举报对象 sm_report
 *
 * @author sm
 */
@Data
@TableName("sm_report")
public class Report implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id")
    private Long id;

    /** 举报人ID */
    private Long reporterId;

    /** 被举报商品ID */
    private Long productId;

    /** 举报原因 */
    private String reason;

    /** 举报说明 */
    private String content;

    /** 处理状态(PENDING/HANDLED/REJECTED) */
    private String status;

    /** 处理人ID */
    private Long handlerId;

    /** 处理备注 */
    private String handleRemark;

    /** 处理时间 */
    private Date handleTime;

    /** 删除标志 */
    @TableLogic
    private String delFlag;

    /** 举报时间 */
    private Date createTime;
}
