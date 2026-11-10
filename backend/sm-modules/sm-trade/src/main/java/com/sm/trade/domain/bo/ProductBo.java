package com.sm.trade.domain.bo;

import com.sm.common.core.validate.AddGroup;
import com.sm.common.core.validate.EditGroup;
import com.sm.trade.domain.Product;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.List;

/**
 * 商品业务对象
 *
 * @author sm
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProductBo extends Product {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 图片URL列表(发布/编辑时提交) */
    private List<String> images;

    /** 价格区间筛选-最低价 */
    private BigDecimal priceMin;

    /** 价格区间筛选-最高价 */
    private BigDecimal priceMax;

    @NotBlank(message = "商品标题不能为空", groups = { AddGroup.class, EditGroup.class })
    @Override
    public String getTitle() {
        return super.getTitle();
    }

    @NotNull(message = "商品分类不能为空", groups = { AddGroup.class })
    @Override
    public Long getCategoryId() {
        return super.getCategoryId();
    }

    @NotNull(message = "商品价格不能为空", groups = { AddGroup.class, EditGroup.class })
    @Override
    public BigDecimal getPrice() {
        return super.getPrice();
    }
}
