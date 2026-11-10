package com.sm.trade.controller.admin;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.sm.common.core.domain.R;
import com.sm.trade.domain.bo.BrandBo;
import com.sm.trade.domain.bo.CategoryBo;
import com.sm.trade.domain.vo.BrandVo;
import com.sm.trade.domain.vo.CategoryVo;
import com.sm.trade.service.IBrandService;
import com.sm.trade.service.ICategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 管理后台:分类与品牌管理
 *
 * @author sm
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/admin")
@SaCheckRole("superadmin")
public class AdminCatalogController {

    private final ICategoryService categoryService;
    private final IBrandService brandService;

    /* ---------- 分类 ---------- */

    @GetMapping("/category/tree")
    public R<List<CategoryVo>> categoryTree() {
        return R.ok(categoryService.selectAdminTree());
    }

    @PostMapping("/category")
    public R<Long> addCategory(@RequestBody CategoryBo bo) {
        return R.ok(categoryService.add(bo));
    }

    @PutMapping("/category")
    public R<Void> editCategory(@RequestBody CategoryBo bo) {
        categoryService.edit(bo);
        return R.ok();
    }

    @DeleteMapping("/category/{id}")
    public R<Void> removeCategory(@PathVariable Long id) {
        categoryService.remove(id);
        return R.ok();
    }

    /* ---------- 品牌 ---------- */

    @GetMapping("/brand/list")
    public R<List<BrandVo>> brandList(@RequestParam(required = false) Long categoryId) {
        return R.ok(brandService.selectList(categoryId));
    }

    @PostMapping("/brand")
    public R<Long> addBrand(@RequestBody BrandBo bo) {
        return R.ok(brandService.add(bo));
    }

    @PutMapping("/brand")
    public R<Void> editBrand(@RequestBody BrandBo bo) {
        brandService.edit(bo);
        return R.ok();
    }

    @DeleteMapping("/brand/{id}")
    public R<Void> removeBrand(@PathVariable Long id) {
        brandService.remove(id);
        return R.ok();
    }
}
