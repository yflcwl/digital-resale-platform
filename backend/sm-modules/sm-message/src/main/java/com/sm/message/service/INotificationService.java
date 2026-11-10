package com.sm.message.service;

import com.sm.common.mybatis.core.page.PageQuery;
import com.sm.common.mybatis.core.page.TableDataInfo;
import com.sm.message.domain.Notification;

/**
 * 站内通知Service接口
 *
 * @author sm
 */
public interface INotificationService {

    /**
     * 我的通知分页
     */
    TableDataInfo<Notification> queryPage(String type, PageQuery pageQuery);

    /**
     * 单条已读
     */
    void markRead(Long id);

    /**
     * 全部已读
     */
    void markAllRead();

    /**
     * 未读数量
     */
    int unreadCount();

    /**
     * 发送站内通知(业务模块调用,落库并尝试实时推送)
     */
    void send(Long userId, String type, String title, String content, String link);
}
