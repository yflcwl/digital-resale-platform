package com.sm.trade.domain.bo;

import com.sm.trade.domain.Brand;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serial;

/**
 * 品牌业务对象
 *
 * @author sm
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BrandBo extends Brand {

    @Serial
    private static final long serialVersionUID = 1L;
}
