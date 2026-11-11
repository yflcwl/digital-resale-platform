package com.sm.web.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * 邮箱验证码对象 sm_email_code
 *
 * @author sm
 */
@Data
@TableName("sm_email_code")
public class EmailCode implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @TableId(value = "id")
    private Long id;

    /** 邮箱 */
    private String email;

    /** 验证码 */
    private String code;

    /** 业务类型(REGISTER/RESET) */
    private String bizType;

    /** 过期时间 */
    private Date expireTime;

    /** 是否已使用(0否 1是) */
    private String used;

    /** 创建时间 */
    private Date createTime;
}
