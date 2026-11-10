-- ----------------------------
-- 管理后台菜单 + 普通用户角色
-- ----------------------------

-- 平台管理目录
insert into sys_menu values('2020', '平台管理', '0', '8', 'admin', null, '', '1', '0', 'M', '0', '0', '', 'build', 103, 1, now(), null, null, '管理后台目录');
insert into sys_menu values('2021', '数据仪表盘', '2020', '1', 'dashboard', 'admin/dashboard/index', '', '1', '0', 'C', '0', '0', '', 'chart', 103, 1, now(), null, null, '统计仪表盘');
insert into sys_menu values('2022', '商品管理', '2020', '2', 'product', 'admin/product/index', '', '1', '0', 'C', '0', '0', '', 'goods', 103, 1, now(), null, null, '全站商品管理');
insert into sys_menu values('2023', '分类管理', '2020', '3', 'category', 'admin/category/index', '', '1', '0', 'C', '0', '0', '', 'tree', 103, 1, now(), null, null, '分类维护');
insert into sys_menu values('2024', '品牌管理', '2020', '4', 'brand', 'admin/brand/index', '', '1', '0', 'C', '0', '0', '', 'star', 103, 1, now(), null, null, '品牌维护');
insert into sys_menu values('2025', '订单管理', '2020', '5', 'order', 'admin/order/index', '', '1', '0', 'C', '0', '0', '', 'order', 103, 1, now(), null, null, '全站订单查询');
insert into sys_menu values('2026', '质检报告', '2020', '6', 'report', 'admin/report/index', '', '1', '0', 'C', '0', '0', '', 'documentation', 103, 1, now(), null, null, '质检报告查阅');

-- 普通用户角色:绑定用户端菜单(2000-2011)
insert into sys_role (role_id, tenant_id, role_name, role_key, role_sort, data_scope, menu_check_strictly, dept_check_strictly, status, del_flag, create_dept, create_by, create_time, remark)
select 100, '000000', '普通用户', 'user', 3, '1', '1', '1', '0', '0', 103, 1, now(), '二手平台普通用户角色'
where not exists (select 1 from sys_role where role_key = 'user');

insert into sys_role_menu (role_id, menu_id)
select 100, menu_id from sys_menu where menu_id between 2000 and 2011
  and not exists (select 1 from sys_role_menu where role_id = 100 and menu_id = sys_menu.menu_id);

-- 把测试买家 buyer01 绑定普通用户角色(如未绑定)
insert into sys_user_role (user_id, role_id)
select u.user_id, 100 from sys_user u
where u.user_name in ('buyer01', 'seller02', 'seller03', 'seller04')
  and not exists (select 1 from sys_user_role ur where ur.user_id = u.user_id and ur.role_id = 100);
