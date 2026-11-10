-- ----------------------------
-- 消息模块菜单:聊天 / 通知中心
-- ----------------------------

insert into sys_menu values('2010', '我的消息', '2000', '10', 'chat', 'message/chat', '', '1', '0', 'C', '0', '0', '', 'message', 103, 1, now(), null, null, '站内聊天(会话/消息)');
insert into sys_menu values('2011', '通知中心', '2000', '11', 'notification', 'message/notification', '', '1', '0', 'C', '0', '0', '', 'bell', 103, 1, now(), null, null, '站内通知列表');
