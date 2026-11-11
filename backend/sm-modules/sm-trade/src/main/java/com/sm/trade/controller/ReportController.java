package com.sm.trade.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.sm.common.core.domain.R;
import com.sm.trade.service.IReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * 商品举报Controller(用户端)
 *
 * @author sm
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/trade/report")
public class ReportController {

    private final IReportService reportService;

    /**
     * 提交举报
     */
    @SaCheckLogin
    @PostMapping
    public R<Void> submit(@RequestBody Map<String, Object> body) {
        reportService.submit(
            Long.valueOf(String.valueOf(body.get("productId"))),
            body.get("reason") == null ? null : String.valueOf(body.get("reason")),
            body.get("content") == null ? null : String.valueOf(body.get("content")));
        return R.ok();
    }
}
