package com.sm.trade.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 用户资料扩展对象 sm_user_profile
 *
 * @author sm
 */
@Data
@TableName("sm_user_profile")
public class UserProfile implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 用户ID(关联sys_user.user_id) */
    @TableId(value = "user_id")
    private Long userId;

    /** 昵称 */
    private String nickname;

    /** 头像URL */
    private String avatar;

    /** 个人简介 */
    private String bio;

    /** 卖家综合评分(0~5) */
    private BigDecimal sellerRating;

    /** 收到评价次数 */
    private Integer ratingCount;

    /** 成交笔数 */
    private Integer dealCount;

    /** 创建时间 */
    private Date createTime;

    /** 更新时间 */
    private Date updateTime;
}
