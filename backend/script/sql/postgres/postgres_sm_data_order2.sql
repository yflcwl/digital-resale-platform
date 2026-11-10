-- ----------------------------
-- 演示数据补充:第二卖家 + admin 的地址与买入订单
-- 目标:地址管理、我买到的、我卖出的 三个页面均有数据
-- ----------------------------

-- 1) 第二个卖家账号 seller02(密码同 admin / seller02 登录用 admin123)
insert into sys_user (user_id, tenant_id, dept_id, user_name, nick_name, user_type, email, phonenumber, sex, avatar, password, status, del_flag, create_dept, create_by, create_time, remark)
select (select coalesce(max(user_id),0)+1 from sys_user), tenant_id, dept_id, 'seller02', '数码小铺', user_type, 'seller02@test.com', '13800000002', '0', null, password, '0', '0', create_dept, 1, now(), '演示卖家二号'
from sys_user where user_name = 'admin';

insert into sm_user_profile (user_id, nickname, avatar, bio, seller_rating, rating_count, deal_count, create_time, update_time)
select user_id, '数码小铺', '', '回收/寄卖一体,支持验机', 4.7, 23, 51, now(), now() from sys_user where user_name = 'seller02'
on conflict (user_id) do nothing;

-- 2) seller02 的商品(17/18 已成交,19 在售)
insert into sm_product (id, seller_id, category_id, brand_id, title, description, price, original_price, condition_self, quality_grade, status, view_count, favorite_count, del_flag, create_dept, create_by, create_time) values
(17, (select user_id from sys_user where user_name='seller02'), 11, 1, 'iPhone 12 64G 蓝色 国行', '换机出售,屏幕无划痕,电池健康 82%,边框轻微使用痕迹。', 1899, 4599, 'GOOD', 'B', 'SOLD', 143, 12, '0', 103, 6, now() - interval '6 day'),
(18, (select user_id from sys_user where user_name='seller02'), 51, 1, 'AirPods 3 代 无线耳机', '自用半年,功能正常,耳机本体成色新,充电盒有细微划痕。', 799, 1399, 'GOOD', 'A', 'SOLD', 208, 19, '0', 103, 6, now() - interval '8 day'),
(19, (select user_id from sys_user where user_name='seller02'), 21, 1, 'iPad Air 4 64G 深空灰', '追剧办公皆宜,屏幕完美,附一支第三方笔,电池健康 90%。', 2399, 4799, 'GOOD', 'A', 'ON_SALE', 96, 8, '0', 103, 6, now() - interval '2 day');

insert into sm_product_image (product_id, url, sort, is_main, create_time) values
(17, '/product/7.jpg', 0, '1', now()),
(18, '/product/13.jpg', 0, '1', now()),
(19, '/product/9.jpg', 0, '1', now());

select setval(pg_get_serial_sequence('sm_product', 'id'), (select max(id) from sm_product));

-- 3) admin 的收货地址(2 条,其中 1 条默认)
insert into sm_user_address (user_id, receiver, phone, province, city, district, detail, is_default, del_flag, create_time, update_time)
select 1, '管理员', '13800000000', '四川省', '成都市', '武侯区', '科华北路 66 号 8 栋 301', '1', '0', now(), now()
where not exists (select 1 from sm_user_address where user_id = 1);

insert into sm_user_address (user_id, receiver, phone, province, city, district, detail, is_default, del_flag, create_time, update_time)
select 1, '管理员(公司)', '13800000000', '四川省', '成都市', '高新区', '天府三街 199 号 A 座 15 层', '0', '0', now(), now()
where (select count(*) from sm_user_address where user_id = 1) < 2;

-- 4) admin 买 seller02 商品 → 订单 D:已发货(admin 登录可确认收货)
insert into sm_order (order_no, buyer_id, seller_id, product_id, product_snapshot, price, status, address_snapshot,
                      express_company, express_no, timeout_time, pay_time, ship_time, del_flag, create_dept, create_by, create_time)
select '202611090810300004', 1, p.seller_id, 17,
       jsonb_build_object('title', p.title, 'image', '/product/7.jpg', 'price', p.price, 'description', p.description),
       1899.00, 'SHIPPED',
       '{"receiver":"管理员","phone":"13800000000","fullAddress":"四川省成都市武侯区科华北路66号8栋301"}',
       '中通快递', 'ZT20261109001', now() - interval '1 day', now() - interval '1 day', now() - interval '20 hour',
       '0', 103, 1, now() - interval '1 day'
from sm_product p where p.id = 17;

insert into sm_order_status_log (order_id, from_status, to_status, operator_type, operator_id, remark, create_time)
select id, null, 'PENDING_PAY', 'BUYER', 1, '买家下单', create_time from sm_order where order_no = '202611090810300004';
insert into sm_order_status_log (order_id, from_status, to_status, operator_type, operator_id, remark, create_time)
select id, 'PENDING_PAY', 'PAID', 'BUYER', 1, '模拟支付成功', pay_time from sm_order where order_no = '202611090810300004';
insert into sm_order_status_log (order_id, from_status, to_status, operator_type, operator_id, remark, create_time)
select id, 'PAID', 'SHIPPED', 'SELLER', seller_id, '卖家发货', ship_time from sm_order where order_no = '202611090810300004';

insert into sm_payment_record (order_id, pay_no, amount, pay_type, status, pay_time)
select id, order_no || '01', price, 'ALIPAY', 'SUCCESS', pay_time from sm_order where order_no = '202611090810300004';

-- 5) admin 买 seller02 商品 → 订单 E:已完成(含评价 5 星)
insert into sm_order (order_no, buyer_id, seller_id, product_id, product_snapshot, price, status, address_snapshot,
                      express_company, express_no, timeout_time, pay_time, ship_time, receive_time, finish_time,
                      del_flag, create_dept, create_by, create_time)
select '202611040920000005', 1, p.seller_id, 18,
       jsonb_build_object('title', p.title, 'image', '/product/13.jpg', 'price', p.price, 'description', p.description),
       799.00, 'FINISHED',
       '{"receiver":"管理员","phone":"13800000000","fullAddress":"四川省成都市武侯区科华北路66号8栋301"}',
       '顺丰速运', 'SF20261104002', now() - interval '6 day', now() - interval '6 day', now() - interval '5 day',
       now() - interval '4 day', now() - interval '4 day', '0', 103, 1, now() - interval '6 day'
from sm_product p where p.id = 18;

insert into sm_order_status_log (order_id, from_status, to_status, operator_type, operator_id, remark, create_time)
select id, null, 'PENDING_PAY', 'BUYER', 1, '买家下单', create_time from sm_order where order_no = '202611040920000005';
insert into sm_order_status_log (order_id, from_status, to_status, operator_type, operator_id, remark, create_time)
select id, 'PENDING_PAY', 'PAID', 'BUYER', 1, '模拟支付成功', pay_time from sm_order where order_no = '202611040920000005';
insert into sm_order_status_log (order_id, from_status, to_status, operator_type, operator_id, remark, create_time)
select id, 'PAID', 'SHIPPED', 'SELLER', seller_id, '卖家发货', ship_time from sm_order where order_no = '202611040920000005';
insert into sm_order_status_log (order_id, from_status, to_status, operator_type, operator_id, remark, create_time)
select id, 'SHIPPED', 'RECEIVED', 'BUYER', 1, '买家确认收货', receive_time from sm_order where order_no = '202611040920000005';
insert into sm_order_status_log (order_id, from_status, to_status, operator_type, operator_id, remark, create_time)
select id, 'RECEIVED', 'REVIEWED', 'BUYER', 1, '买家评价', finish_time from sm_order where order_no = '202611040920000005';
insert into sm_order_status_log (order_id, from_status, to_status, operator_type, operator_id, remark, create_time)
select id, 'REVIEWED', 'FINISHED', 'SYSTEM', null, '系统自动完成', finish_time from sm_order where order_no = '202611040920000005';

insert into sm_payment_record (order_id, pay_no, amount, pay_type, status, pay_time)
select id, order_no || '01', price, 'WECHAT', 'SUCCESS', pay_time from sm_order where order_no = '202611040920000005';

insert into sm_order_review (order_id, reviewer_id, reviewee_id, rating, content, create_time)
select id, 1, seller_id, 5, '耳机成色与描述一致,包装仔细,好评!', finish_time
from sm_order where order_no = '202611040920000005';

-- 6) 相关通知
insert into sm_notification (user_id, type, title, content, link, is_read, create_time)
select 1, 'ORDER', '商品已发货', '快递单号:ZT20261109001', '/trade/orderDetail?id=' || id, '0', ship_time
from sm_order where order_no = '202611090810300004';
insert into sm_notification (user_id, type, title, content, link, is_read, create_time)
select seller_id, 'ORDER', '收到新评价', '买家给出了 5 星评价', '/trade/orderDetail?id=' || id, '0', finish_time
from sm_order where order_no = '202611040920000005';

-- 7) seller02 评分更新
update sm_user_profile
set seller_rating = 4.8, rating_count = 24
where user_id = (select user_id from sys_user where user_name = 'seller02');
