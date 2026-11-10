package com.sm.message.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.sm.common.core.utils.StringUtils;
import com.sm.common.json.utils.JsonUtils;
import com.sm.common.mybatis.core.page.PageQuery;
import com.sm.common.mybatis.core.page.TableDataInfo;
import com.sm.common.satoken.utils.LoginHelper;
import com.sm.common.websocket.utils.WebSocketUtils;
import com.sm.message.domain.Notification;
import com.sm.message.mapper.NotificationMapper;
import com.sm.message.service.INotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 站内通知Service业务层处理
 *
 * @author sm
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class NotificationServiceImpl implements INotificationService {

    private final NotificationMapper baseMapper;

    @Override
    public TableDataInfo<Notification> queryPage(String type, PageQuery pageQuery) {
        LambdaQueryWrapper<Notification> lqw = Wrappers.lambdaQuery();
        lqw.eq(Notification::getUserId, LoginHelper.getUserId());
        lqw.eq(StringUtils.isNotBlank(type), Notification::getType, type);
        lqw.orderByAsc(Notification::getIsRead);
        lqw.orderByDesc(Notification::getCreateTime);
        return TableDataInfo.build(baseMapper.selectPage(pageQuery.build(), lqw));
    }

    @Override
    public void markRead(Long id) {
        Notification notification = baseMapper.selectById(id);
        if (notification == null || !LoginHelper.getUserId().equals(notification.getUserId())) {
            return;
        }
        LambdaUpdateWrapper<Notification> uw = Wrappers.lambdaUpdate();
        uw.set(Notification::getIsRead, "1");
        uw.set(Notification::getReadTime, new Date());
        uw.eq(Notification::getId, id);
        baseMapper.update(null, uw);
    }

    @Override
    public void markAllRead() {
        LambdaUpdateWrapper<Notification> uw = Wrappers.lambdaUpdate();
        uw.set(Notification::getIsRead, "1");
        uw.set(Notification::getReadTime, new Date());
        uw.eq(Notification::getUserId, LoginHelper.getUserId());
        uw.eq(Notification::getIsRead, "0");
        baseMapper.update(null, uw);
    }

    @Override
    public int unreadCount() {
        return Math.toIntExact(baseMapper.selectCount(Wrappers.<Notification>lambdaQuery()
            .eq(Notification::getUserId, LoginHelper.getUserId())
            .eq(Notification::getIsRead, "0")));
    }

    @Override
    public void send(Long userId, String type, String title, String content, String link) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setType(type);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setLink(link);
        notification.setIsRead("0");
        notification.setCreateTime(new Date());
        baseMapper.insert(notification);
        pushNotifyEvent(userId, title);
    }

    private void pushNotifyEvent(Long userId, String title) {
        try {
            Map<String, Object> event = new LinkedHashMap<>();
            event.put("type", "notify");
            event.put("title", title);
            event.put("unread", unreadCountOf(userId));
            WebSocketUtils.sendMessage(userId, JsonUtils.toJsonString(event));
        } catch (Exception e) {
            // 推送失败不影响通知落库
            log.warn("通知推送失败 userId={}", userId, e);
        }
    }

    private int unreadCountOf(Long userId) {
        return Math.toIntExact(baseMapper.selectCount(Wrappers.<Notification>lambdaQuery()
            .eq(Notification::getUserId, userId)
            .eq(Notification::getIsRead, "0")));
    }
}
