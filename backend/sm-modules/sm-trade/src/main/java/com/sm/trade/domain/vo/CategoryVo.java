package com.sm.trade.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import com.sm.trade.domain.Category;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;

/**
 * 商品分类视图对象(树)
 *
 * @author sm
 */
@AutoMapper(target = Category.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class CategoryVo extends Category {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 子分类 */
    private List<CategoryVo> children = new ArrayList<>();
}
