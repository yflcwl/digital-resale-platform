package com.sm.trade.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 卖家信息视图对象(商品详情展示)
 *
 * @author sm
 */
@Data
public class SellerInfoVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 卖家用户ID */
    private Long userId;

    /** 昵称 */
    private String nickname;

    /** 头像 */
    private String avatar;

    /** 综合评分 */
    private BigDecimal sellerRating;

    /** 成交笔数 */
    private Integer dealCount;
}
