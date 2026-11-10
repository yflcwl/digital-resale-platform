package com.sm.trade.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sm.common.mybatis.core.domain.BaseEntity;
import com.sm.trade.handler.JsonbTypeHandler;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.Date;

/**
 * 订单对象 sm_order
 *
 * @author sm
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sm_order", autoResultMap = true)
public class Order extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(value = "id")
    private Long id;

    /** 订单号 */
    private String orderNo;

    /** 买家用户ID */
    private Long buyerId;

    /** 卖家用户ID */
    private Long sellerId;

    /** 商品ID */
    private Long productId;

    /** 商品快照(JSON) */
    @TableField(typeHandler = JsonbTypeHandler.class)
    private String productSnapshot;

    /** 实付金额 */
    private BigDecimal price;

    /** 订单状态 */
    private String status;

    /** 收货地址快照(JSON) */
    @TableField(typeHandler = JsonbTypeHandler.class)
    private String addressSnapshot;

    /** 快递公司 */
    private String expressCompany;

    /** 快递单号 */
    private String expressNo;

    /** 超时自动取消时间 */
    private Date timeoutTime;

    /** 支付时间 */
    private Date payTime;

    /** 发货时间 */
    private Date shipTime;

    /** 收货时间 */
    private Date receiveTime;

    /** 完成时间 */
    private Date finishTime;

    /** 取消时间 */
    private Date cancelTime;

    /** 删除标志(0正常 2删除) */
    @TableLogic
    private String delFlag;
}
