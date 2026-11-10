-- ----------------------------
-- 演示数据扩充
-- 覆盖:更多卖家/商品、待支付与已取消订单、收藏、浏览历史、轮播图、质检报告
-- ----------------------------

-- 1) 新增两个卖家账号(密码同 admin / admin123)
insert into sys_user (user_id, tenant_id, dept_id, user_name, nick_name, user_type, email, phonenumber, sex, avatar, password, status, del_flag, create_dept, create_by, create_time, remark)
select (select coalesce(max(user_id),0)+1 from sys_user), tenant_id, dept_id, 'seller03', '乐享数码', user_type, 'seller03@test.com', '13800000003', '0', null, password, '0', '0', create_dept, 1, now(), '演示卖家三号'
from sys_user where user_name = 'admin';

insert into sys_user (user_id, tenant_id, dept_id, user_name, nick_name, user_type, email, phonenumber, sex, avatar, password, status, del_flag, create_dept, create_by, create_time, remark)
select (select coalesce(max(user_id),0)+1 from sys_user), tenant_id, dept_id, 'seller04', '极客二手', user_type, 'seller04@test.com', '13800000004', '0', null, password, '0', '0', create_dept, 1, now(), '演示卖家四号'
from sys_user where user_name = 'admin';

insert into sm_user_profile (user_id, nickname, avatar, bio, seller_rating, rating_count, deal_count, create_time, update_time)
select user_id, nick_name, '', '品牌数码回收与二手寄卖', 4.9, 67, 210, now(), now() from sys_user where user_name in ('seller03','seller04')
on conflict (user_id) do nothing;

-- 2) 新商品(20-31),覆盖各分类与卖家
insert into sm_product (id, seller_id, category_id, brand_id, title, description, price, original_price, condition_self, quality_grade, status, view_count, favorite_count, del_flag, create_dept, create_by, create_time) values
(20, (select user_id from sys_user where user_name='seller02'), 11, 1, 'iPhone 11 128G 黑色 国行',        '备用机出售,无拆无修,电池健康 85%,屏幕完好。',                       2299, 5999, 'GOOD',      'B', 'ON_SALE', 187, 14, '0', 103, 6, now() - interval '3 day'),
(21, (select user_id from sys_user where user_name='seller03'), 12, 3, '一加 12 16+512 岩板绿',            '旗舰性能,游戏办公两相宜,全套配件,成色极新。',                       2999, 4799, 'EXCELLENT', 'S', 'ON_SALE', 312, 41, '0', 103, 7, now() - interval '1 day'),
(22, (select user_id from sys_user where user_name='seller03'), 31, 8, '联想小新 Pro16 锐龙版 R7',          '大屏轻薄本,大学自用,无磕碰,键盘无油光。',                           4599, 6499, 'GOOD',      'A', 'ON_SALE', 156, 22, '0', 103, 7, now() - interval '2 day'),
(23, (select user_id from sys_user where user_name='seller03'), 52, 12, '索尼 WH-1000XM5 头戴降噪',         '降噪天花板,耳罩完好,附原装收纳包。',                                 1599, 2899, 'GOOD',      'A', 'ON_SALE', 274, 38, '0', 103, 7, now() - interval '4 day'),
(24, (select user_id from sys_user where user_name='seller03'), 42, 14, '尼康 Z5 全画幅微单 24-50 套机',    '入门全画幅首选,快门 8 千次,传感器干净。',                             6999, 9999, 'GOOD',      'B', 'ON_SALE', 98,  9,  '0', 103, 7, now() - interval '6 day'),
(25, (select user_id from sys_user where user_name='seller04'), 12, 3, 'Redmi K70 16+256 晴雪',            '性能小钢炮,自用三个月,屏幕无划痕,附赠钢化膜。',                     1799, 2999, 'EXCELLENT', 'A', 'ON_SALE', 221, 27, '0', 103, 8, now() - interval '2 day'),
(26, (select user_id from sys_user where user_name='seller04'), 31, 1, 'MacBook Pro 14 M1 Pro 16+512',     '程序员自用,电池循环 120 次,外壳无磕碰。',                           7999, 14999, 'GOOD',     'A', 'ON_SALE', 341, 52, '0', 103, 8, now() - interval '5 day'),
(27, (select user_id from sys_user where user_name='seller04'), 41, 12, 'GoPro HERO11 Black 运动相机',      '骑行自用,防水壳齐全,成色良好。',                                     1499, 2998, 'GOOD',      'B', 'ON_SALE', 133, 11, '0', 103, 8, now() - interval '7 day'),
(28, (select user_id from sys_user where user_name='seller04'), 71, 1, 'Apple Watch Ultra 49mm GPS+蜂窝',  '户外神器,钛金属表壳,含海洋表带。',                                   2999, 6299, 'EXCELLENT', 'S', 'ON_SALE', 189, 33, '0', 103, 8, now() - interval '1 day'),
(29, 1, 51, 2, '华为 FreeBuds Pro 3 真无线降噪', '佩戴舒适,降噪优秀,充电盒全新。',                                                 799,  1499, 'EXCELLENT', 'S', 'ON_SALE', 264, 30, '0', 103, 1, now() - interval '3 day'),
(30, 1, 82, 9, '戴尔 U2723QE 27 英寸 4K 显示器', '设计制图神器,IPS Black 面板,无坏点,附原装支架。',                                2499, 4199, 'GOOD',      'A', 'ON_SALE', 147, 18, '0', 103, 1, now() - interval '4 day'),
(31, 1, 81, 19, '罗技 G502 HERO 游戏鼠标',      '经典游戏鼠,手感扎实,微动正常,附配重块。',                                         199,  399,  'GOOD',      'B', 'ON_SALE', 88,  6,  '0', 103, 1, now() - interval '9 day');

insert into sm_product_image (product_id, url, sort, is_main, create_time)
select id, '/product/' || (6 + (id % 11)) || '.jpg', 0, '1', now() from sm_product where id between 20 and 31;

select setval(pg_get_serial_sequence('sm_product', 'id'), (select max(id) from sm_product));

-- 3) 待支付订单(buyer01 买商品 20 → 商品锁定)
update sm_product set status = 'LOCKED' where id = 20;

insert into sm_order (order_no, buyer_id, seller_id, product_id, product_snapshot, price, status, address_snapshot,
                      timeout_time, del_flag, create_dept, create_by, create_time)
select '202611100830000006', 5, p.seller_id, 20,
       jsonb_build_object('title', p.title, 'image', '/product/15.jpg', 'price', p.price, 'description', p.description),
       2299.00, 'PENDING_PAY',
       '{"receiver":"测试买家","phone":"13800000001","fullAddress":"四川省成都市高新区天府大道100号"}',
       now() + interval '25 minute', '0', 103, 5, now() - interval '5 minute'
from sm_product p where p.id = 20;

insert into sm_order_status_log (order_id, from_status, to_status, operator_type, operator_id, remark, create_time)
select id, null, 'PENDING_PAY', 'BUYER', 5, '买家下单', create_time from sm_order where order_no = '202611100830000006';

insert into sm_notification (user_id, type, title, content, link, is_read, create_time)
select seller_id, 'ORDER', '收到新订单', '买家已下单,等待付款', '/trade/orderDetail?id=' || id, '0', create_time
from sm_order where order_no = '202611100830000006';

-- 4) 已取消订单(buyer01 买商品 21 后取消,商品仍可售)
insert into sm_order (order_no, buyer_id, seller_id, product_id, product_snapshot, price, status, address_snapshot,
                      timeout_time, cancel_time, del_flag, create_dept, create_by, create_time)
select '202611090715000007', 5, p.seller_id, 21,
       jsonb_build_object('title', p.title, 'image', '/product/5.jpg', 'price', p.price, 'description', p.description),
       2999.00, 'CANCELED',
       '{"receiver":"测试买家","phone":"13800000001","fullAddress":"四川省成都市高新区天府大道100号"}',
       now() - interval '1 day', now() - interval '1 day' + interval '12 minute', '0', 103, 5, now() - interval '1 day'
from sm_product p where p.id = 21;

insert into sm_order_status_log (order_id, from_status, to_status, operator_type, operator_id, remark, create_time)
select id, null, 'PENDING_PAY', 'BUYER', 5, '买家下单', create_time from sm_order where order_no = '202611090715000007';
insert into sm_order_status_log (order_id, from_status, to_status, operator_type, operator_id, remark, create_time)
select id, 'PENDING_PAY', 'CANCELED', 'BUYER', 5, '买家取消', cancel_time from sm_order where order_no = '202611090715000007';

-- 5) 收藏数据(admin 5 条 / buyer01 6 条)
insert into sm_product_favorite (user_id, product_id, create_time)
select 1, id, now() - (row_number() over (order by id)) * interval '2 hour'
from (select id from sm_product where status = 'ON_SALE' order by id limit 5) t
on conflict do nothing;

insert into sm_product_favorite (user_id, product_id, create_time)
select 5, id, now() - (row_number() over (order by id)) * interval '3 hour'
from (select id from sm_product where status = 'ON_SALE' order by id desc limit 6) t
on conflict do nothing;

-- 6) 浏览历史(admin 8 条 / buyer01 10 条)
insert into sm_product_browse (user_id, product_id, browse_time)
select 1, id, now() - (row_number() over (order by id)) * interval '1 hour'
from (select id from sm_product where status in ('ON_SALE','SOLD') order by id limit 8) t;

insert into sm_product_browse (user_id, product_id, browse_time)
select 5, id, now() - (row_number() over (order by id desc)) * interval '40 minute'
from (select id from sm_product where status in ('ON_SALE','SOLD') order by id desc limit 10) t;

-- 7) 首页轮播图
insert into sm_carousel (title, image_url, link, sort, status, del_flag, create_dept, create_by, create_time) values
('开学季数码焕新', '/product/3.jpg', '/trade/market', 1, '0', '0', 103, 1, now()),
('二手也有好成色', '/product/8.jpg', '/trade/market', 2, '0', '0', 103, 1, now()),
('智能质检 值得信赖', '/product/12.jpg', '/trade/market', 3, '0', '0', 103, 1, now());

-- 8) 为所有在售商品批量生成质检任务/报告/缺陷(供应商演示用)
DO $$
DECLARE
    r RECORD;
    v_task_id BIGINT;
    v_report_id BIGINT;
    v_brand VARCHAR(50);
    v_score NUMERIC;
    v_total_defects INT;
BEGIN
    FOR r IN SELECT p.id, p.seller_id, p.price, p.quality_grade, p.brand_id, b.name AS brand_name
             FROM sm_product p LEFT JOIN sm_brand b ON p.brand_id = b.id
             WHERE p.status = 'ON_SALE'
    LOOP
        INSERT INTO sm_inspection_task (product_id, round_no, status, photo_urls, model_name, confidence, duration_ms, del_flag, create_by, create_time)
        VALUES (r.id, 1, 'SUCCESS',
                '["/product/1.jpg","/product/2.jpg","/product/3.jpg","/product/4.jpg","/product/5.jpg"]'::jsonb,
                'qwen-vl-max', 85 + random() * 10, 3000 + floor(random() * 3000), '0', r.seller_id,
                now() - interval '1 day')
        RETURNING id INTO v_task_id;

        v_score := CASE coalesce(r.quality_grade, 'A')
                        WHEN 'S' THEN 95 + random() * 4
                        WHEN 'A' THEN 85 + random() * 9
                        WHEN 'B' THEN 70 + random() * 14
                        WHEN 'C' THEN 55 + random() * 14
                        ELSE 40 + random() * 14
                    END;

        INSERT INTO sm_inspection_report (task_id, product_id, quality_grade, brand_model, defect_summary,
                                          price_min, price_max, confidence, status, del_flag, create_by, create_time)
        VALUES (v_task_id, r.id, coalesce(r.quality_grade, 'A'),
                coalesce(r.brand_name, '') || ' 已识别机型',
                CASE coalesce(r.quality_grade, 'A')
                    WHEN 'S' THEN '外观几乎无瑕疵,功能正常'
                    WHEN 'A' THEN '存在轻微使用痕迹,不影响使用'
                    WHEN 'B' THEN '边框与后盖有可见磨损及轻微磕碰'
                    WHEN 'C' THEN '多处明显磨损,屏幕有细划痕'
                    ELSE '存在较明显缺陷,建议谨慎购买'
                END,
                round(r.price * 0.90, 2), round(r.price * 1.05, 2),
                round(v_score, 1), 'VALID', '0', r.seller_id, now() - interval '1 day')
        RETURNING id INTO v_report_id;

        v_total_defects := CASE coalesce(r.quality_grade, 'A') WHEN 'S' THEN 0 WHEN 'A' THEN 1 WHEN 'B' THEN 2 ELSE 3 END;

        IF v_total_defects >= 1 THEN
            INSERT INTO sm_inspection_defect (report_id, defect_type, position, severity, description, sort)
            VALUES (v_report_id, 'SCRATCH', '屏幕', 'MILD', '屏幕存在细微划痕,亮屏下较难察觉', 0);
        END IF;
        IF v_total_defects >= 2 THEN
            INSERT INTO sm_inspection_defect (report_id, defect_type, position, severity, description, sort)
            VALUES (v_report_id, 'DENT', '边框', 'MODERATE', '边框一角有轻微磕碰', 1);
        END IF;
        IF v_total_defects >= 3 THEN
            INSERT INTO sm_inspection_defect (report_id, defect_type, position, severity, description, sort)
            VALUES (v_report_id, 'PAINT_LOSS', '后盖', 'MODERATE', '后盖边缘掉漆', 2);
        END IF;

        UPDATE sm_product SET latest_report_id = v_report_id WHERE id = r.id;
    END LOOP;
END $$;
