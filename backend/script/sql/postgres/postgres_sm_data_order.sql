-- ----------------------------
-- 演示订单数据(历史订单,覆盖各状态)
-- 说明:直接插入已支付后的状态,不触发延迟消息;
--       对应商品状态同步为 SOLD,保持数据一致。
-- ----------------------------

-- 商品 2(iPhone 14 Pro)→ 订单 A:已支付待发货
update sm_product set status = 'SOLD' where id = 2;

insert into sm_order (order_no, buyer_id, seller_id, product_id, product_snapshot, price, status, address_snapshot,
                      timeout_time, pay_time, del_flag, create_dept, create_by, create_time)
values ('202611080930120001', 5, 1, 2,
        '{"title":"iPhone 14 Pro 256G 暗夜紫","image":"/product/2.jpg","price":6799,"description":"成色极佳,几乎全新,保修到明年3月"}',
        6799.00, 'PAID',
        '{"receiver":"测试买家","phone":"13800000001","fullAddress":"四川省成都市高新区天府大道100号"}',
        now() - interval '2 day', now() - interval '2 day', '0', 103, 5, now() - interval '2 day');

insert into sm_order_status_log (order_id, from_status, to_status, operator_type, operator_id, remark, create_time)
select id, null, 'PENDING_PAY', 'BUYER', 5, '买家下单', create_time from sm_order where order_no = '202611080930120001';
insert into sm_order_status_log (order_id, from_status, to_status, operator_type, operator_id, remark, create_time)
select id, 'PENDING_PAY', 'PAID', 'BUYER', 5, '模拟支付成功', pay_time from sm_order where order_no = '202611080930120001';

insert into sm_payment_record (order_id, pay_no, amount, pay_type, status, pay_time)
select id, order_no || '01', price, 'WECHAT', 'SUCCESS', pay_time from sm_order where order_no = '202611080930120001';

-- 商品 3(华为 Mate 60 Pro)→ 订单 B:已发货待收货
update sm_product set status = 'SOLD' where id = 3;

insert into sm_order (order_no, buyer_id, seller_id, product_id, product_snapshot, price, status, address_snapshot,
                      express_company, express_no, timeout_time, pay_time, ship_time, del_flag, create_dept, create_by, create_time)
values ('202611070815300002', 5, 1, 3,
        '{"title":"华为 Mate 60 Pro 512G 雅川青","image":"/product/3.jpg","price":5299,"description":"自用半年,无维修史"}',
        5299.00, 'SHIPPED',
        '{"receiver":"测试买家","phone":"13800000001","fullAddress":"四川省成都市高新区天府大道100号"}',
        '顺丰速运', 'SF3098765432', now() - interval '3 day', now() - interval '3 day', now() - interval '2 day',
        '0', 103, 5, now() - interval '3 day');

insert into sm_order_status_log (order_id, from_status, to_status, operator_type, operator_id, remark, create_time)
select id, null, 'PENDING_PAY', 'BUYER', 5, '买家下单', create_time from sm_order where order_no = '202611070815300002';
insert into sm_order_status_log (order_id, from_status, to_status, operator_type, operator_id, remark, create_time)
select id, 'PENDING_PAY', 'PAID', 'BUYER', 5, '模拟支付成功', pay_time from sm_order where order_no = '202611070815300002';
insert into sm_order_status_log (order_id, from_status, to_status, operator_type, operator_id, remark, create_time)
select id, 'PAID', 'SHIPPED', 'SELLER', 1, '卖家发货', ship_time from sm_order where order_no = '202611070815300002';

insert into sm_payment_record (order_id, pay_no, amount, pay_type, status, pay_time)
select id, order_no || '01', price, 'ALIPAY', 'SUCCESS', pay_time from sm_order where order_no = '202611070815300002';

-- 商品 4(小米 14 Ultra)→ 订单 C:已完成(含评价)
update sm_product set status = 'SOLD' where id = 4;

insert into sm_order (order_no, buyer_id, seller_id, product_id, product_snapshot, price, status, address_snapshot,
                      express_company, express_no, timeout_time, pay_time, ship_time, receive_time, finish_time,
                      del_flag, create_dept, create_by, create_time)
values ('202611051420000003', 5, 1, 4,
        '{"title":"小米 14 Ultra 16+512 白色","image":"/product/4.jpg","price":3899,"description":"影像旗舰,退烧出售"}',
        3899.00, 'FINISHED',
        '{"receiver":"测试买家","phone":"13800000001","fullAddress":"四川省成都市高新区天府大道100号"}',
        '中通快递', 'ZT7788990011', now() - interval '5 day', now() - interval '5 day', now() - interval '4 day',
        now() - interval '2 day', now() - interval '2 day', '0', 103, 5, now() - interval '5 day');

insert into sm_order_status_log (order_id, from_status, to_status, operator_type, operator_id, remark, create_time)
select id, null, 'PENDING_PAY', 'BUYER', 5, '买家下单', create_time from sm_order where order_no = '202611051420000003';
insert into sm_order_status_log (order_id, from_status, to_status, operator_type, operator_id, remark, create_time)
select id, 'PENDING_PAY', 'PAID', 'BUYER', 5, '模拟支付成功', pay_time from sm_order where order_no = '202611051420000003';
insert into sm_order_status_log (order_id, from_status, to_status, operator_type, operator_id, remark, create_time)
select id, 'PAID', 'SHIPPED', 'SELLER', 1, '卖家发货', ship_time from sm_order where order_no = '202611051420000003';
insert into sm_order_status_log (order_id, from_status, to_status, operator_type, operator_id, remark, create_time)
select id, 'SHIPPED', 'RECEIVED', 'BUYER', 5, '买家确认收货', receive_time from sm_order where order_no = '202611051420000003';
insert into sm_order_status_log (order_id, from_status, to_status, operator_type, operator_id, remark, create_time)
select id, 'RECEIVED', 'REVIEWED', 'BUYER', 5, '买家评价', finish_time from sm_order where order_no = '202611051420000003';
insert into sm_order_status_log (order_id, from_status, to_status, operator_type, operator_id, remark, create_time)
select id, 'REVIEWED', 'FINISHED', 'SYSTEM', null, '系统自动完成', finish_time from sm_order where order_no = '202611051420000003';

insert into sm_payment_record (order_id, pay_no, amount, pay_type, status, pay_time)
select id, order_no || '01', price, 'ALIPAY', 'SUCCESS', pay_time from sm_order where order_no = '202611051420000003';

insert into sm_order_review (order_id, reviewer_id, reviewee_id, rating, content, create_time)
select id, 5, 1, 4, '手机成色不错,与描述一致,卖家发货及时。', now() - interval '2 day'
from sm_order where order_no = '202611051420000003';

-- 通知数据
insert into sm_notification (user_id, type, title, content, link, is_read, create_time)
select 1, 'ORDER', '订单已支付', '买家已付款,请尽快发货', '/trade/orderDetail?id=' || id, '1', pay_time
from sm_order where order_no = '202611080930120001';
insert into sm_notification (user_id, type, title, content, link, is_read, create_time)
select 5, 'ORDER', '商品已发货', '快递单号:SF3098765432', '/trade/orderDetail?id=' || id, '0', ship_time
from sm_order where order_no = '202611070815300002';
insert into sm_notification (user_id, type, title, content, link, is_read, create_time)
select 1, 'ORDER', '收到新评价', '买家给出了 4 星评价', '/trade/orderDetail?id=' || id, '0', now() - interval '2 day'
from sm_order where order_no = '202611051420000003';

-- 更新卖家评分(4 星一条评价)
update sm_user_profile
set seller_rating = 4.8, rating_count = rating_count + 1, deal_count = deal_count + 3
where user_id = 1;
