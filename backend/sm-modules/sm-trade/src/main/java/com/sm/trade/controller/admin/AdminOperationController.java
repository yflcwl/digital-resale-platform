package com.sm.trade.controller.admin;

import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.sm.common.core.domain.R;
import com.sm.common.mybatis.core.page.PageQuery;
import com.sm.common.mybatis.core.page.TableDataInfo;
import com.sm.trade.domain.Carousel;
import com.sm.trade.domain.Report;
import com.sm.trade.mapper.CarouselMapper;
import com.sm.trade.service.IReportService;
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

import java.util.Map;

/**
 * 管理后台:举报处理 + 轮播图管理
 *
 * @author sm
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/admin")
@SaCheckRole("superadmin")
public class AdminOperationController {

    private final IReportService reportService;
    private final CarouselMapper carouselMapper;

    /* ---------- 举报 ---------- */

    @GetMapping("/report/page")
    public TableDataInfo<Report> reportPage(@RequestParam(required = false) String status, PageQuery pageQuery) {
        return reportService.queryAdminPage(status, pageQuery);
    }

    @PostMapping("/report/{id}/handle")
    public R<Void> handleReport(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String status = body.get("status") == null ? null : String.valueOf(body.get("status"));
        String remark = body.get("remark") == null ? null : String.valueOf(body.get("remark"));
        boolean offShelf = Boolean.parseBoolean(String.valueOf(body.getOrDefault("offShelf", false)));
        reportService.handle(id, status, remark, offShelf);
        return R.ok();
    }

    /* ---------- 轮播图 ---------- */

    @GetMapping("/carousel/list")
    public R<java.util.List<Carousel>> carouselList() {
        return R.ok(carouselMapper.selectList(Wrappers.<Carousel>lambdaQuery()
            .orderByAsc(Carousel::getSort)));
    }

    @PostMapping("/carousel")
    public R<Long> addCarousel(@RequestBody Carousel carousel) {
        carousel.setId(null);
        carouselMapper.insert(carousel);
        return R.ok(carousel.getId());
    }

    @PutMapping("/carousel")
    public R<Void> editCarousel(@RequestBody Carousel carousel) {
        Carousel update = new Carousel();
        update.setId(carousel.getId());
        update.setTitle(carousel.getTitle());
        update.setImageUrl(carousel.getImageUrl());
        update.setLink(carousel.getLink());
        update.setSort(carousel.getSort());
        update.setStatus(carousel.getStatus());
        carouselMapper.updateById(update);
        return R.ok();
    }

    @DeleteMapping("/carousel/{id}")
    public R<Void> removeCarousel(@PathVariable Long id) {
        carouselMapper.deleteById(id);
        return R.ok();
    }
}
