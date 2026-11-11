package com.sm.trade.domain.vo;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 卖家主页信息
 *
 * @author sm
 */
@Data
public class SellerHomeVo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 卖家用户ID */
    private Long userId;

    /** 昵称 */
    private String nickname;

    /** 头像 */
    private String avatar;

    /** 简介 */
    private String bio;

    /** 综合评分 */
    private BigDecimal sellerRating;

    /** 评价次数 */
    private Integer ratingCount;

    /** 成交笔数 */
    private Integer dealCount;

    /** 在售商品数 */
    private Long onSaleCount;
}
