-- 卖家主页菜单(隐藏,从商品详情进入)
insert into sys_menu values('2014', '卖家主页', '2000', '14', 'seller', 'trade/seller/index', '', '1', '0', 'C', '1', '0', '', '#', 103, 1, now(), null, null, '卖家店铺页(隐藏)');

insert into sys_role_menu (role_id, menu_id)
select 100, 2014 where not exists (select 1 from sys_role_menu where role_id = 100 and menu_id = 2014);
