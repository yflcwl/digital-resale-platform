package com.sm.trade.domain.vo;

import com.sm.trade.domain.Brand;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 品牌视图对象
 *
 * @author sm
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BrandVo extends Brand {

    @Serial
    private static final long serialVersionUID = 1L;
}
