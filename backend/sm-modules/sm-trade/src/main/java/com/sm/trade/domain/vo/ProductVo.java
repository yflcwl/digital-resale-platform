package com.sm.trade.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import com.sm.trade.domain.Product;
import com.sm.trade.domain.ProductImage;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.List;

/**
 * 商品视图对象
 *
 * @author sm
 */
@AutoMapper(target = Product.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class ProductVo extends Product {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 图片列表 */
    private List<ProductImage> images;

    /** 分类名称 */
    private String categoryName;

    /** 品牌名称 */
    private String brandName;

    /** 卖家昵称 */
    private String sellerNickname;

    /** 卖家头像 */
    private String sellerAvatar;

    /** 卖家综合评分 */
    private BigDecimal sellerRating;

    /** 当前用户是否已收藏 */
    private Boolean favorite;
}
