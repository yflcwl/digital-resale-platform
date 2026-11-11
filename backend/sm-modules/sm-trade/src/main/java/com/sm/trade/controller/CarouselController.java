package com.sm.trade.controller;

import cn.dev33.satoken.annotation.SaIgnore;
import com.sm.common.core.domain.R;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.sm.trade.domain.Carousel;
import com.sm.trade.mapper.CarouselMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 首页轮播图Controller(公开)
 *
 * @author sm
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/trade/carousel")
public class CarouselController {

    private final CarouselMapper carouselMapper;

    /**
     * 展示中的轮播图列表
     */
    @SaIgnore
    @GetMapping("/list")
    public R<List<Carousel>> list() {
        return R.ok(carouselMapper.selectList(Wrappers.<Carousel>lambdaQuery()
            .eq(Carousel::getStatus, "0")
            .orderByAsc(Carousel::getSort)));
    }
}
