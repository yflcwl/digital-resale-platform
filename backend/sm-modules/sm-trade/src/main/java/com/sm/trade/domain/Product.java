package com.sm.trade.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import com.sm.common.mybatis.core.domain.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.math.BigDecimal;

/**
 * 商品对象 sm_product
 *
 * @author sm
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sm_product")
public class Product extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(value = "id")
    private Long id;

    /** 卖家用户ID */
    private Long sellerId;

    /** 分类ID */
    private Long categoryId;

    /** 品牌ID(历史字段,已迁移为 brandName) */
    private Long brandId;

    /** 品牌名称(字典 product_brand) */
    private String brandName;

    /** 商品标题 */
    private String title;

    /** 商品描述 */
    private String description;

    /** 售价 */
    private BigDecimal price;

    /** 原价 */
    private BigDecimal originalPrice;

    /** 新旧程度自评(EXCELLENT/GOOD/FAIR/POOR) */
    private String conditionSelf;

    /** 质检成色等级(S/A/B/C/D) */
    private String qualityGrade;

    /** 商品状态 */
    private String status;

    /** 浏览次数 */
    private Integer viewCount;

    /** 收藏次数 */
    private Integer favoriteCount;

    /** 最新有效质检报告ID */
    private Long latestReportId;

    /** 是否首页精选(0否 1是) */
    private String isFeatured;

    /** 删除标志(0正常 2删除) */
    @TableLogic
    private String delFlag;
}
