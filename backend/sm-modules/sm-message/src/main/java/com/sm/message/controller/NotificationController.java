package com.sm.message.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.sm.common.core.domain.R;
import com.sm.common.mybatis.core.page.PageQuery;
import com.sm.common.mybatis.core.page.TableDataInfo;
import com.sm.message.domain.Notification;
import com.sm.message.service.INotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 站内通知Controller
 *
 * @author sm
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/message/notification")
public class NotificationController {

    private final INotificationService notificationService;

    /**
     * 我的通知分页(type 可选:ORDER/INSPECTION/SYSTEM)
     */
    @SaCheckLogin
    @GetMapping("/page")
    public TableDataInfo<Notification> page(@RequestParam(required = false) String type, PageQuery pageQuery) {
        return notificationService.queryPage(type, pageQuery);
    }

    /**
     * 单条已读
     */
    @SaCheckLogin
    @PostMapping("/{id}/read")
    public R<Void> markRead(@PathVariable Long id) {
        notificationService.markRead(id);
        return R.ok();
    }

    /**
     * 全部已读
     */
    @SaCheckLogin
    @PostMapping("/read-all")
    public R<Void> markAllRead() {
        notificationService.markAllRead();
        return R.ok();
    }

    /**
     * 未读数量
     */
    @SaCheckLogin
    @GetMapping("/unread-count")
    public R<Integer> unreadCount() {
        return R.ok(notificationService.unreadCount());
    }
}
