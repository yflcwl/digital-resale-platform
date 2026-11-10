package com.sm.trade.domain.vo;

import io.github.linpeilie.annotations.AutoMapper;
import com.sm.trade.domain.Brand;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 品牌视图对象
 *
 * @author sm
 */
@AutoMapper(target = Brand.class)
@Data
@EqualsAndHashCode(callSuper = true)
public class BrandVo extends Brand {

    @Serial
    private static final long serialVersionUID = 1L;
}
