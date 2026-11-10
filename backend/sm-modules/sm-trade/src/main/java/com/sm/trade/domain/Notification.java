package com.sm.trade.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 站内通知对象 sm_notification
 *
 * @author sm
 */
@Data
@TableName("sm_notification")
public class Notification implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(value = "id")
    private Long id;

    /** 接收人ID */
    private Long userId;

    /** 通知类型(ORDER/INSPECTION/SYSTEM) */
    private String type;

    /** 通知标题 */
    private String title;

    /** 内容摘要 */
    private String content;

    /** 跳转链接 */
    private String link;

    /** 是否已读(0否 1是) */
    private String isRead;

    /** 阅读时间 */
    private Date readTime;

    /** 通知时间 */
    private Date createTime;
}
