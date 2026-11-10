-- ----------------------------
-- 二手交易平台 用户端菜单
-- 商品模块
-- ----------------------------

insert into sys_menu values('2000', '二手市场', '0', '9', 'trade', null, '', '1', '0', 'M', '0', '0', '', 'shopping', 103, 1, now(), null, null, '二手市场目录');

-- 商品市场(在售商品列表)
insert into sys_menu values('2001', '商品市场', '2000', '1', 'market', 'trade/product/index', '', '1', '0', 'C', '0', '0', '', 'goods', 103, 1, now(), null, null, '在售商品列表');
-- 商品详情(隐藏)
insert into sys_menu values('2002', '商品详情', '2000', '2', 'detail', 'trade/product/detail', '', '1', '0', 'C', '1', '0', '', '#', 103, 1, now(), null, null, '商品详情(隐藏)');
-- 发布商品(隐藏)
insert into sys_menu values('2003', '发布商品', '2000', '3', 'publish', 'trade/product/publish', '', '1', '0', 'C', '1', '0', '', '#', 103, 1, now(), null, null, '发布/编辑商品(隐藏)');
-- 我的商品
insert into sys_menu values('2004', '我的商品', '2000', '4', 'my', 'trade/product/my', '', '1', '0', 'C', '0', '0', '', 'list', 103, 1, now(), null, null, '卖家商品管理');
-- 我的收藏
insert into sys_menu values('2005', '我的收藏', '2000', '5', 'favorite', 'trade/product/favorite', '', '1', '0', 'C', '0', '0', '', 'star', 103, 1, now(), null, null, '商品收藏列表');
