package com.sm.trade.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 分类-品牌关联对象 sm_category_brand
 *
 * @author sm
 */
@Data
@TableName("sm_category_brand")
public class CategoryBrand implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 主键 */
    @TableId(value = "id")
    private Long id;

    /** 分类ID */
    private Long categoryId;

    /** 品牌ID */
    private Long brandId;
}
