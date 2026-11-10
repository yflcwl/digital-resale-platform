package com.sm.inspection.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaIgnore;
import com.sm.common.core.domain.R;
import com.sm.common.idempotent.annotation.RepeatSubmit;
import com.sm.inspection.service.IInspectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 智能质检Controller(用户端)
 *
 * @author sm
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/inspection")
public class InspectionController {

    private final IInspectionService inspectionService;

    /**
     * 拍照引导清单
     */
    @SaIgnore
    @GetMapping("/guide/{categoryId}")
    public R<List<Map<String, Object>>> guide(@PathVariable Long categoryId) {
        return R.ok(inspectionService.guide(categoryId));
    }

    /**
     * 提交质检
     */
    @SaCheckLogin
    @RepeatSubmit
    @PostMapping("/task")
    public R<Long> submit(@RequestBody Map<String, Object> body) {
        Long productId = Long.valueOf(String.valueOf(body.get("productId")));
        List<String> photos = toStringList(body.get("photoUrls"));
        return R.ok(inspectionService.submit(productId, photos));
    }

    /**
     * 补拍提交
     */
    @SaCheckLogin
    @RepeatSubmit
    @PostMapping("/task/{id}/reshoot")
    public R<Long> reshoot(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        return R.ok(inspectionService.reshoot(id, toStringList(body.get("photoUrls"))));
    }

    /**
     * 任务状态查询(前端轮询)
     */
    @SaCheckLogin
    @GetMapping("/task/{id}")
    public R<Map<String, Object>> queryTask(@PathVariable Long id) {
        return R.ok(inspectionService.queryTask(id));
    }

    /**
     * 报告详情(公开,买家可在商品详情查看)
     */
    @SaIgnore
    @GetMapping("/report/{id}")
    public R<Map<String, Object>> report(@PathVariable Long id) {
        return R.ok(inspectionService.queryReport(id));
    }

    /**
     * 商品最新有效报告(公开)
     */
    @SaIgnore
    @GetMapping("/product/{productId}/report")
    public R<Map<String, Object>> productReport(@PathVariable Long productId) {
        return R.ok(inspectionService.queryProductReport(productId));
    }

    /**
     * 报告反馈
     */
    @SaCheckLogin
    @PostMapping("/report/{id}/feedback")
    public R<Void> feedback(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        inspectionService.feedback(id, body.get("content") == null ? null : String.valueOf(body.get("content")));
        return R.ok();
    }

    @SuppressWarnings("unchecked")
    private List<String> toStringList(Object value) {
        if (value instanceof List<?> list) {
            return list.stream().map(String::valueOf).toList();
        }
        return List.of();
    }
}
