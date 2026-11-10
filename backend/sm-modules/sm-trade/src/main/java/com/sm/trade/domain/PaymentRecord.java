package com.sm.trade.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 模拟支付流水对象 sm_payment_record
 *
 * @author sm
 */
@Data
@TableName("sm_payment_record")
public class PaymentRecord implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(value = "id")
    private Long id;

    /** 订单ID */
    private Long orderId;

    /** 支付流水号 */
    private String payNo;

    /** 支付金额 */
    private BigDecimal amount;

    /** 支付方式(ALIPAY/WECHAT) */
    private String payType;

    /** 支付状态(SUCCESS/FAILED) */
    private String status;

    /** 支付时间 */
    private Date payTime;

    /** 备注 */
    private String remark;
}
