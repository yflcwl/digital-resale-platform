package com.sm.trade.controller;

import com.sm.common.core.domain.R;
import com.sm.trade.domain.vo.BrandVo;
import com.sm.trade.service.IBrandService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 品牌Controller(用户端)
 *
 * @author sm
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/trade/brand")
public class BrandController {

    private final IBrandService brandService;

    /**
     * 品牌列表(可按分类筛选)
     */
    @GetMapping("/list")
    public R<List<BrandVo>> list(@RequestParam(required = false) Long categoryId) {
        return R.ok(brandService.selectList(categoryId));
    }
}
