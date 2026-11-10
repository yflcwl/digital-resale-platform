-- ----------------------------
-- 交易订单 / 收货地址 菜单
-- ----------------------------

-- 我的订单(我买到/我卖出)
insert into sys_menu values('2006', '我的订单', '2000', '6', 'order', 'trade/order/index', '', '1', '0', 'C', '0', '0', '', 'order', 103, 1, now(), null, null, '订单列表(买入/卖出)');
-- 订单详情(隐藏)
insert into sys_menu values('2007', '订单详情', '2000', '7', 'orderDetail', 'trade/order/detail', '', '1', '0', 'C', '1', '0', '', '#', 103, 1, now(), null, null, '订单详情(隐藏)');
-- 模拟支付(隐藏)
insert into sys_menu values('2008', '模拟支付', '2000', '8', 'pay', 'trade/order/pay', '', '1', '0', 'C', '1', '0', '', '#', 103, 1, now(), null, null, '模拟支付页(隐藏)');
-- 收货地址
insert into sys_menu values('2009', '收货地址', '2000', '9', 'address', 'trade/address/index', '', '1', '0', 'C', '0', '0', '', 'location', 103, 1, now(), null, null, '个人收货地址管理');
