package com.sm.trade.controller;

import com.sm.common.core.domain.R;
import com.sm.trade.domain.vo.CategoryVo;
import com.sm.trade.service.ICategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 商品分类Controller(用户端)
 *
 * @author sm
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/trade/category")
public class CategoryController {

    private final ICategoryService categoryService;

    /**
     * 分类树
     */
    @GetMapping("/tree")
    public R<List<CategoryVo>> tree() {
        return R.ok(categoryService.selectTree());
    }
}
