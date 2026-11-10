package com.sm.trade.controller.admin;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.sm.common.core.domain.R;
import com.sm.common.core.validate.QueryGroup;
import com.sm.common.mybatis.core.page.PageQuery;
import com.sm.common.mybatis.core.page.TableDataInfo;
import com.sm.trade.domain.bo.ProductBo;
import com.sm.trade.domain.vo.ProductVo;
import com.sm.trade.service.IProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 管理后台:商品管理
 *
 * @author sm
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/admin/product")
@SaCheckRole("superadmin")
public class AdminProductController {

    private final IProductService productService;

    /**
     * 商品分页(全部状态)
     */
    @GetMapping("/page")
    public TableDataInfo<ProductVo> page(@Validated(QueryGroup.class) ProductBo bo, PageQuery pageQuery) {
        return productService.adminPage(bo, pageQuery);
    }

    /**
     * 强制下架
     */
    @PostMapping("/{id}/off-shelf")
    public R<Void> offShelf(@PathVariable Long id) {
        productService.adminOffShelf(id);
        return R.ok();
    }

    /**
     * 删除商品
     */
    @DeleteMapping("/{id}")
    public R<Void> remove(@PathVariable Long id) {
        productService.adminRemove(id);
        return R.ok();
    }
}
