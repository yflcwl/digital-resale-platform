-- ----------------------------
-- 演示数据:批量订单(admin 视角:我买到的 20 条 / 我卖出的 20 条)
-- 同时扩充收货地址
-- ----------------------------

-- 1) 收货地址扩充
insert into sm_user_address (user_id, receiver, phone, province, city, district, detail, is_default, del_flag, create_time, update_time)
select 1, d.receiver, d.phone, d.province, d.city, d.district, d.detail, '0', '0', now(), now()
from (values
    ('管理员(老家)', '13800000000', '四川省', '绵阳市', '涪城区', '临园路东段 18 号 2 单元 501'),
    ('管理员(仓库)', '13800000000', '四川省', '成都市', '双流区', '物流大道 88 号 3 号库'),
    ('管理员(学校)', '13800000000', '四川省', '成都市', '郫都区', '学院路 200 号 12 栋 402')
) as d(receiver, phone, province, city, district, detail)
where (select count(*) from sm_user_address where user_id = 1) < 5;

insert into sm_user_address (user_id, receiver, phone, province, city, district, detail, is_default, del_flag, create_time, update_time)
select 5, d.receiver, d.phone, d.province, d.city, d.district, d.detail, '0', '0', now(), now()
from (values
    ('张三(公司)', '13800000001', '四川省', '成都市', '高新区', '软件园 B 区 7 栋 901'),
    ('张三(老家)', '13800000001', '重庆市', '重庆市', '渝北区', '金开大道 1000 号 6-2'),
    ('张三(宿舍)', '13800000001', '四川省', '成都市', '郫都区', '学院路 200 号 5 栋 301')
) as d(receiver, phone, province, city, district, detail)
where (select count(*) from sm_user_address where user_id = 5) < 6;

-- 2) 造 40 个历史成交商品(不展示在市场,仅用于历史订单)
DO $$
DECLARE
    v_titles text[] := array[
        'iPhone 12 Pro 128G 石墨色', 'iPhone 13 mini 128G 粉色', '华为 P50 Pro 256G 雪域白',
        '小米 13 12+256 远山蓝', 'OPPO Find X5 Pro 256G 黑釉', 'vivo X90 12+256 华夏红',
        '三星 S22 Ultra 256G 白色', '荣耀 Magic4 Pro 256G 亮黑', 'iPad mini 6 64G 紫色',
        'iPad Air 5 256G 蓝色', '华为 MatePad 11 128G 曜石灰', '小米平板 6 Pro 256G 黑色',
        'MacBook Air M1 8+256 银色', '联想 YOGA 14s 锐龙版', 'ThinkPad X1 Carbon 2021',
        '戴尔 XPS 13 9310', '华硕灵耀 14 酷睿版', '索尼 A7C 全画幅微单',
        '佳能 EOS R8 套机', '富士 X-S20 15-45 套机', '尼康 Z30 双镜头套机',
        'Bose QC Earbuds II', '索尼 WF-1000XM5', 'JBL TUNE 760NC',
        'Apple Watch S8 45mm', '华为 Watch GT4 46mm', '小米手环 8 Pro',
        'Switch OLED 白色 喷射战士限定', 'Xbox Series S 512G', 'PS5 数字版 国行',
        '罗技 MX Keys 键盘', '安克 100W 氮化镓充电器', '贝尔金 65W 双口充电器',
        'Apple Pencil 二代', 'MagSafe 磁吸充电器', 'AirTag 四件套',
        '索尼 A6400 单机身', '佳能 G7X Mark III', '大疆 Pocket 3 全能套装',
        'GoPro HERO12 Black'
    ];
    v_img int;
    v_seller bigint;
    v_price numeric;
    v_i int;
BEGIN
    FOR v_i IN 1..40 LOOP
        v_img := 1 + (v_i % 16);
        v_price := 300 + floor(random() * 8000);
        v_seller := CASE
            WHEN v_i <= 20 THEN
                CASE v_i % 3 WHEN 0 THEN (select user_id from sys_user where user_name='seller02')
                             WHEN 1 THEN (select user_id from sys_user where user_name='seller03')
                             ELSE (select user_id from sys_user where user_name='seller04') END
            ELSE 1
        END;

        INSERT INTO sm_product (id, seller_id, category_id, brand_id, title, description, price, original_price,
                                condition_self, quality_grade, status, view_count, favorite_count, del_flag,
                                create_dept, create_by, create_time)
        VALUES (31 + v_i, v_seller, 1 + (v_i % 8), 1 + (v_i % 20), v_titles[v_i],
                '历史成交商品(演示数据)', v_price, v_price * 2,
                CASE v_i % 4 WHEN 0 THEN 'EXCELLENT' WHEN 1 THEN 'GOOD' WHEN 2 THEN 'GOOD' ELSE 'FAIR' END,
                (array['S','A','A','B','B','C'])[1 + (v_i % 6)], 'SOLD',
                floor(random() * 400), floor(random() * 50), '0', 103, v_seller,
                now() - (v_i || ' day')::interval);

        INSERT INTO sm_product_image (product_id, url, sort, is_main, create_time)
        VALUES (31 + v_i, '/product/' || v_img || '.jpg', 0, '1', now());
    END LOOP;
END $$;

select setval(pg_get_serial_sequence('sm_product', 'id'), (select max(id) from sm_product));

-- 3) 生成 20 条 admin 买入 + 20 条 admin 卖出订单
DO $$
DECLARE
    v_statuses text[] := array['FINISHED','FINISHED','FINISHED','FINISHED','RECEIVED','SHIPPED','PAID','CANCELED'];
    v_buyers bigint[] := array[5, 5, 6, 7, 8];
    v_i int;
    v_status text;
    v_p record;
    v_order_id bigint;
    v_buyer bigint;
    v_address text;
    v_pay_time timestamp;
    v_ship_time timestamp;
    v_recv_time timestamp;
    v_fin_time timestamp;
    v_no text;
BEGIN
    -- admin 买入(商品 32-51,卖家为其他账号)
    FOR v_i IN 1..20 LOOP
        SELECT * INTO v_p FROM sm_product WHERE id = 31 + v_i;
        v_status := v_statuses[1 + (v_i % 8)];
        v_address := '{"receiver":"管理员","phone":"13800000000","fullAddress":"四川省成都市武侯区科华北路66号8栋301"}';
        v_pay_time := now() - ((25 - v_i) || ' day')::interval;
        v_ship_time := v_pay_time + interval '1 day';
        v_recv_time := v_ship_time + interval '2 day';
        v_fin_time := v_recv_time + interval '1 day';
        v_no := to_char(v_pay_time, 'YYYYMMDD') || lpad((1000 + v_i)::text, 4, '0') || '1';

        INSERT INTO sm_order (order_no, buyer_id, seller_id, product_id, product_snapshot, price, status,
                              address_snapshot, express_company, express_no, timeout_time,
                              pay_time, ship_time, receive_time, finish_time, cancel_time,
                              del_flag, create_dept, create_by, create_time)
        VALUES (v_no, 1, v_p.seller_id, v_p.id,
                jsonb_build_object('title', v_p.title, 'image', '/product/' || (1 + (v_i % 16)) || '.jpg',
                                   'price', v_p.price, 'description', v_p.description),
                v_p.price, v_status, v_address::jsonb,
                CASE WHEN v_status IN ('SHIPPED','RECEIVED','FINISHED') THEN '顺丰速运' END,
                CASE WHEN v_status IN ('SHIPPED','RECEIVED','FINISHED') THEN 'SF' || lpad((8800000 + v_i)::text, 8, '0') END,
                v_pay_time + interval '30 minute',
                CASE WHEN v_status NOT IN ('PENDING_PAY','CANCELED') THEN v_pay_time END,
                CASE WHEN v_status IN ('SHIPPED','RECEIVED','FINISHED') THEN v_ship_time END,
                CASE WHEN v_status IN ('RECEIVED','FINISHED') THEN v_recv_time END,
                CASE WHEN v_status = 'FINISHED' THEN v_fin_time END,
                CASE WHEN v_status = 'CANCELED' THEN v_pay_time + interval '20 minute' END,
                '0', 103, 1, v_pay_time - interval '1 hour')
        RETURNING id INTO v_order_id;

        INSERT INTO sm_order_status_log (order_id, from_status, to_status, operator_type, operator_id, remark, create_time)
        VALUES (v_order_id, null, 'PENDING_PAY', 'BUYER', 1, '买家下单', v_pay_time - interval '1 hour');
        IF v_status <> 'PENDING_PAY' AND v_status <> 'CANCELED' THEN
            INSERT INTO sm_order_status_log (order_id, from_status, to_status, operator_type, operator_id, remark, create_time)
            VALUES (v_order_id, 'PENDING_PAY', 'PAID', 'BUYER', 1, '模拟支付成功', v_pay_time);
            INSERT INTO sm_payment_record (order_id, pay_no, amount, pay_type, status, pay_time)
            VALUES (v_order_id, v_no || '01', v_p.price, 'ALIPAY', 'SUCCESS', v_pay_time);
        END IF;
        IF v_status IN ('SHIPPED','RECEIVED','FINISHED') THEN
            INSERT INTO sm_order_status_log (order_id, from_status, to_status, operator_type, operator_id, remark, create_time)
            VALUES (v_order_id, 'PAID', 'SHIPPED', 'SELLER', v_p.seller_id, '卖家发货', v_ship_time);
        END IF;
        IF v_status IN ('RECEIVED','FINISHED') THEN
            INSERT INTO sm_order_status_log (order_id, from_status, to_status, operator_type, operator_id, remark, create_time)
            VALUES (v_order_id, 'SHIPPED', 'RECEIVED', 'BUYER', 1, '买家确认收货', v_recv_time);
        END IF;
        IF v_status = 'FINISHED' THEN
            INSERT INTO sm_order_status_log (order_id, from_status, to_status, operator_type, operator_id, remark, create_time)
            VALUES (v_order_id, 'RECEIVED', 'REVIEWED', 'BUYER', 1, '买家评价', v_fin_time);
            INSERT INTO sm_order_status_log (order_id, from_status, to_status, operator_type, operator_id, remark, create_time)
            VALUES (v_order_id, 'REVIEWED', 'FINISHED', 'SYSTEM', null, '系统自动完成', v_fin_time);
            IF v_i % 2 = 0 THEN
                INSERT INTO sm_order_review (order_id, reviewer_id, reviewee_id, rating, content, create_time)
                VALUES (v_order_id, 1, v_p.seller_id, 5, '商品与描述一致,交易愉快,推荐!', v_fin_time);
            END IF;
        END IF;
        IF v_status = 'CANCELED' THEN
            INSERT INTO sm_order_status_log (order_id, from_status, to_status, operator_type, operator_id, remark, create_time)
            VALUES (v_order_id, 'PENDING_PAY', 'CANCELED', 'BUYER', 1, '买家取消', v_pay_time + interval '20 minute');
        END IF;
    END LOOP;

    -- admin 卖出(商品 52-71,买家在 buyer01/seller02/03/04 间轮转)
    FOR v_i IN 1..20 LOOP
        SELECT * INTO v_p FROM sm_product WHERE id = 51 + v_i;
        v_status := v_statuses[1 + ((v_i + 3) % 8)];
        v_buyer := v_buyers[1 + (v_i % 5)];
        v_address := '{"receiver":"买家' || v_buyer || '","phone":"1380000000' || v_buyer || '","fullAddress":"四川省成都市高新区天府大道100号"}';
        v_pay_time := now() - ((22 - v_i) || ' day')::interval;
        v_ship_time := v_pay_time + interval '1 day';
        v_recv_time := v_ship_time + interval '2 day';
        v_fin_time := v_recv_time + interval '1 day';
        v_no := to_char(v_pay_time, 'YYYYMMDD') || lpad((2000 + v_i)::text, 4, '0') || '2';

        INSERT INTO sm_order (order_no, buyer_id, seller_id, product_id, product_snapshot, price, status,
                              address_snapshot, express_company, express_no, timeout_time,
                              pay_time, ship_time, receive_time, finish_time, cancel_time,
                              del_flag, create_dept, create_by, create_time)
        VALUES (v_no, v_buyer, 1, v_p.id,
                jsonb_build_object('title', v_p.title, 'image', '/product/' || (1 + ((v_i + 3) % 16)) || '.jpg',
                                   'price', v_p.price, 'description', v_p.description),
                v_p.price, v_status, v_address::jsonb,
                CASE WHEN v_status IN ('SHIPPED','RECEIVED','FINISHED') THEN '中通快递' END,
                CASE WHEN v_status IN ('SHIPPED','RECEIVED','FINISHED') THEN 'ZT' || lpad((9900000 + v_i)::text, 8, '0') END,
                v_pay_time + interval '30 minute',
                CASE WHEN v_status NOT IN ('PENDING_PAY','CANCELED') THEN v_pay_time END,
                CASE WHEN v_status IN ('SHIPPED','RECEIVED','FINISHED') THEN v_ship_time END,
                CASE WHEN v_status IN ('RECEIVED','FINISHED') THEN v_recv_time END,
                CASE WHEN v_status = 'FINISHED' THEN v_fin_time END,
                CASE WHEN v_status = 'CANCELED' THEN v_pay_time + interval '20 minute' END,
                '0', 103, v_buyer, v_pay_time - interval '1 hour')
        RETURNING id INTO v_order_id;

        INSERT INTO sm_order_status_log (order_id, from_status, to_status, operator_type, operator_id, remark, create_time)
        VALUES (v_order_id, null, 'PENDING_PAY', 'BUYER', v_buyer, '买家下单', v_pay_time - interval '1 hour');
        IF v_status <> 'PENDING_PAY' AND v_status <> 'CANCELED' THEN
            INSERT INTO sm_order_status_log (order_id, from_status, to_status, operator_type, operator_id, remark, create_time)
            VALUES (v_order_id, 'PENDING_PAY', 'PAID', 'BUYER', v_buyer, '模拟支付成功', v_pay_time);
            INSERT INTO sm_payment_record (order_id, pay_no, amount, pay_type, status, pay_time)
            VALUES (v_order_id, v_no || '01', v_p.price, 'WECHAT', 'SUCCESS', v_pay_time);
        END IF;
        IF v_status IN ('SHIPPED','RECEIVED','FINISHED') THEN
            INSERT INTO sm_order_status_log (order_id, from_status, to_status, operator_type, operator_id, remark, create_time)
            VALUES (v_order_id, 'PAID', 'SHIPPED', 'SELLER', 1, '卖家发货', v_ship_time);
        END IF;
        IF v_status IN ('RECEIVED','FINISHED') THEN
            INSERT INTO sm_order_status_log (order_id, from_status, to_status, operator_type, operator_id, remark, create_time)
            VALUES (v_order_id, 'SHIPPED', 'RECEIVED', 'BUYER', v_buyer, '买家确认收货', v_recv_time);
        END IF;
        IF v_status = 'FINISHED' THEN
            INSERT INTO sm_order_status_log (order_id, from_status, to_status, operator_type, operator_id, remark, create_time)
            VALUES (v_order_id, 'RECEIVED', 'REVIEWED', 'BUYER', v_buyer, '买家评价', v_fin_time);
            INSERT INTO sm_order_status_log (order_id, from_status, to_status, operator_type, operator_id, remark, create_time)
            VALUES (v_order_id, 'REVIEWED', 'FINISHED', 'SYSTEM', null, '系统自动完成', v_fin_time);
            IF v_i % 2 = 1 THEN
                INSERT INTO sm_order_review (order_id, reviewer_id, reviewee_id, rating, content, create_time)
                VALUES (v_order_id, v_buyer, 1, 4 + (v_i % 2), '发货很快,包装严实,成色满意。', v_fin_time);
            END IF;
        END IF;
        IF v_status = 'CANCELED' THEN
            INSERT INTO sm_order_status_log (order_id, from_status, to_status, operator_type, operator_id, remark, create_time)
            VALUES (v_order_id, 'PENDING_PAY', 'CANCELED', 'BUYER', v_buyer, '买家取消', v_pay_time + interval '20 minute');
        END IF;
    END LOOP;
END $$;

-- 4) 通知补充
insert into sm_notification (user_id, type, title, content, link, is_read, create_time)
select 1, 'ORDER', '收到新订单', '您的商品有新订单成交', '/trade/orderDetail?id=' || id,
       case when random() > 0.5 then '1' else '0' end, create_time + interval '1 minute'
from sm_order where seller_id = 1 order by create_time desc limit 10;
