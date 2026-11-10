package com.sm.trade.controller.admin;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.sm.common.core.domain.R;
import com.sm.trade.domain.bo.CategoryBo;
import com.sm.trade.domain.vo.CategoryVo;
import com.sm.trade.service.ICategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理后台:分类管理
 *
 * <p>品牌已迁移至字典管理(系统管理 -> 字典管理 -> product_brand),不再提供独立管理接口。</p>
 *
 * @author sm
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/admin/category")
@SaCheckRole("superadmin")
public class AdminCatalogController {

    private final ICategoryService categoryService;

    @GetMapping("/tree")
    public R<List<CategoryVo>> categoryTree() {
        return R.ok(categoryService.selectAdminTree());
    }

    @PostMapping
    public R<Long> addCategory(@RequestBody CategoryBo bo) {
        return R.ok(categoryService.add(bo));
    }

    @PutMapping
    public R<Void> editCategory(@RequestBody CategoryBo bo) {
        categoryService.edit(bo);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    public R<Void> removeCategory(@PathVariable Long id) {
        categoryService.remove(id);
        return R.ok();
    }
}
