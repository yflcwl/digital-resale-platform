package com.sm.trade.domain.bo;

import io.github.linpeilie.annotations.AutoMapper;
import com.sm.trade.domain.Category;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 商品分类业务对象
 *
 * @author sm
 */
@AutoMapper(target = Category.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class CategoryBo extends Category {

    @Serial
    private static final long serialVersionUID = 1L;
}
