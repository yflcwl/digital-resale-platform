package com.sm.trade.domain.bo;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 订单业务对象(下单/支付/发货/评价)
 *
 * @author sm
 */
@Data
public class OrderBo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 下单:商品ID */
    @NotNull(message = "商品ID不能为空")
    private Long productId;

    /** 下单:收货地址ID */
    @NotNull(message = "收货地址不能为空")
    private Long addressId;

    /** 支付方式(ALIPAY/WECHAT) */
    private String payType;

    /** 发货:快递公司 */
    private String expressCompany;

    /** 发货:快递单号 */
    private String expressNo;

    /** 评价:星级 1~5 */
    @Min(value = 1, message = "评分最低1星")
    @Max(value = 5, message = "评分最高5星")
    private Integer rating;

    /** 评价:内容 */
    private String content;

    /** 查询:状态筛选 */
    private String status;
}
