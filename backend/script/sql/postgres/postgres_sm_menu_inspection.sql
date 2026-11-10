-- ----------------------------
-- 智能质检菜单
-- ----------------------------

-- 质检发起页(隐藏,从"我的商品"进入)
insert into sys_menu values('2012', '发起质检', '2000', '12', 'inspectionTask', 'inspection/task', '', '1', '0', 'C', '1', '0', '', '#', 103, 1, now(), null, null, '质检提交(隐藏)');
-- 质检报告页(隐藏,通知/商品详情跳转)
insert into sys_menu values('2013', '质检报告', '2000', '13', 'inspectionReport', 'inspection/report', '', '1', '0', 'C', '1', '0', '', '#', 103, 1, now(), null, null, '质检报告(隐藏)');

-- 普通用户角色同步可见
insert into sys_role_menu (role_id, menu_id)
select 100, menu_id from sys_menu where menu_id in (2012, 2013)
  and not exists (select 1 from sys_role_menu where role_id = 100 and menu_id = sys_menu.menu_id);
