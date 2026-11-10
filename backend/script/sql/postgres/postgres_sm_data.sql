-- ----------------------------
-- 二手交易平台 演示数据
-- 分类 / 品牌 / 分类-品牌关联 / 商品 / 用户资料
-- ----------------------------

-- ============ 分类(三级) ============
insert into sm_category (id, parent_id, name, icon, level, sort, status, del_flag, create_dept, create_by, create_time) values
(1,  0,   '手机',       '', 1, 1, '0', '0', 103, 1, now()),
(2,  0,   '平板',       '', 1, 2, '0', '0', 103, 1, now()),
(3,  0,   '笔记本',     '', 1, 3, '0', '0', 103, 1, now()),
(4,  0,   '相机',       '', 1, 4, '0', '0', 103, 1, now()),
(5,  0,   '耳机',       '', 1, 5, '0', '0', 103, 1, now()),
(6,  0,   '游戏机',     '', 1, 6, '0', '0', 103, 1, now()),
(7,  0,   '智能穿戴',   '', 1, 7, '0', '0', 103, 1, now()),
(8,  0,   '配件',       '', 1, 8, '0', '0', 103, 1, now()),
(11, 1,   'iPhone',     '', 2, 1, '0', '0', 103, 1, now()),
(12, 1,   '安卓手机',   '', 2, 2, '0', '0', 103, 1, now()),
(21, 2,   'iPad',       '', 2, 1, '0', '0', 103, 1, now()),
(22, 2,   '安卓平板',   '', 2, 2, '0', '0', 103, 1, now()),
(31, 3,   '轻薄本',     '', 2, 1, '0', '0', 103, 1, now()),
(32, 3,   '游戏本',     '', 2, 2, '0', '0', 103, 1, now()),
(41, 4,   '微单/无反',  '', 2, 1, '0', '0', 103, 1, now()),
(42, 4,   '单反',       '', 2, 2, '0', '0', 103, 1, now()),
(51, 5,   '真无线耳机', '', 2, 1, '0', '0', 103, 1, now()),
(52, 5,   '头戴式耳机', '', 2, 2, '0', '0', 103, 1, now()),
(61, 6,   '掌机',       '', 2, 1, '0', '0', 103, 1, now()),
(62, 6,   '家用主机',   '', 2, 2, '0', '0', 103, 1, now()),
(71, 7,   '智能手表',   '', 2, 1, '0', '0', 103, 1, now()),
(72, 7,   '智能手环',   '', 2, 2, '0', '0', 103, 1, now()),
(81, 8,   '键鼠外设',   '', 2, 1, '0', '0', 103, 1, now()),
(82, 8,   '充电配件',   '', 2, 2, '0', '0', 103, 1, now());

-- ============ 品牌 ============
insert into sm_brand (id, name, logo, status, sort, del_flag, create_dept, create_by, create_time) values
(1,  '苹果',   '', '0', 1,  '0', 103, 1, now()),
(2,  '华为',   '', '0', 2,  '0', 103, 1, now()),
(3,  '小米',   '', '0', 3,  '0', 103, 1, now()),
(4,  'OPPO',   '', '0', 4,  '0', 103, 1, now()),
(5,  'vivo',   '', '0', 5,  '0', 103, 1, now()),
(6,  '三星',   '', '0', 6,  '0', 103, 1, now()),
(7,  '荣耀',   '', '0', 7,  '0', 103, 1, now()),
(8,  '联想',   '', '0', 8,  '0', 103, 1, now()),
(9,  '戴尔',   '', '0', 9,  '0', 103, 1, now()),
(10, '华硕',   '', '0', 10, '0', 103, 1, now()),
(11, '微软',   '', '0', 11, '0', 103, 1, now()),
(12, '索尼',   '', '0', 12, '0', 103, 1, now()),
(13, '佳能',   '', '0', 13, '0', 103, 1, now()),
(14, '尼康',   '', '0', 14, '0', 103, 1, now()),
(15, '富士',   '', '0', 15, '0', 103, 1, now()),
(16, 'Bose',   '', '0', 16, '0', 103, 1, now()),
(17, 'JBL',    '', '0', 17, '0', 103, 1, now()),
(18, '任天堂', '', '0', 18, '0', 103, 1, now()),
(19, '罗技',   '', '0', 19, '0', 103, 1, now()),
(20, '安克',   '', '0', 20, '0', 103, 1, now());

-- ============ 分类-品牌关联 ============
insert into sm_category_brand (category_id, brand_id) values
-- 手机:苹果/华为/小米/OPPO/vivo/三星/荣耀
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7),
-- 平板:苹果/华为/小米/三星
(2, 1), (2, 2), (2, 3), (2, 6),
-- 笔记本:苹果/联想/戴尔/华硕/微软
(3, 1), (3, 8), (3, 9), (3, 10), (3, 11),
-- 相机:索尼/佳能/尼康/富士
(4, 12), (4, 13), (4, 14), (4, 15),
-- 耳机:苹果/Bose/索尼/JBL
(5, 1), (5, 16), (5, 12), (5, 17),
-- 游戏机:任天堂/微软/索尼
(6, 18), (6, 11), (6, 12),
-- 穿戴:苹果/华为/小米
(7, 1), (7, 2), (7, 3),
-- 配件:罗技/安克/苹果
(8, 19), (8, 20), (8, 1);

-- ============ 用户资料(admin 演示卖家) ============
insert into sm_user_profile (user_id, nickname, avatar, bio, seller_rating, rating_count, deal_count, create_time, update_time) values
(1, '数码老玩家', '', '专注二手数码回收与出售,所有商品均已质检', 4.9, 86, 132, now(), now())
on conflict (user_id) do update set nickname = excluded.nickname, seller_rating = excluded.seller_rating;

-- ============ 商品(在售) ============
insert into sm_product (seller_id, category_id, brand_id, title, description, price, original_price, condition_self, quality_grade, status, view_count, favorite_count, latest_report_id, del_flag, create_dept, create_by, create_time) values
(1, 11, 1,  'iPhone 13 128G 星光色 国行',       '个人自用机,无拆无修,电池健康度 87%,边框两处轻微磕碰,屏幕完美无划痕,附原装充电线。', 2699, 5999, 'GOOD',      'A', 'ON_SALE', 156, 12, null, '0', 103, 1, now() - interval '3 day'),
(1, 11, 1,  'iPhone 14 Pro 256G 暗夜紫',        '成色极佳,几乎全新,保修到明年 3 月,全套配件发票齐全。',                                           6799, 8899, 'EXCELLENT', 'S', 'ON_SALE', 268, 45, null, '0', 103, 1, now() - interval '2 day'),
(1, 12, 2,  '华为 Mate 60 Pro 512G 雅川青',      '自用半年,无维修史,屏幕贴膜使用,后盖无划痕,支持验货。',                                             5299, 6999, 'GOOD',      'A', 'ON_SALE', 189, 23, null, '0', 103, 1, now() - interval '4 day'),
(1, 12, 3,  '小米 14 Ultra 16+512 白色',         '影像旗舰,退烧出售,附赠两个手机壳与 90W 充电套装。',                                                 3899, 6499, 'FAIR',      'B', 'ON_SALE', 97,  8,  null, '0', 103, 1, now() - interval '5 day'),
(1, 21, 1,  'iPad Pro 11 英寸 M2 256G 深空灰',   '学习办公首选,屏幕无划痕,机身有轻微使用痕迹,附 Apple Pencil 二代。',                                4499, 6799, 'GOOD',      'A', 'ON_SALE', 143, 19, null, '0', 103, 1, now() - interval '6 day'),
(1, 31, 1,  'MacBook Air M2 13 英寸 16+512',     '轻办公神器,循环充电 60 次,外观 95 新,键盘无油光,附原装充电器。',                                    6299, 9499, 'EXCELLENT', 'S', 'ON_SALE', 210, 33, null, '0', 103, 1, now() - interval '2 day'),
(1, 32, 8,  '联想拯救者 Y9000P i9+4060',          '游戏本,3A 大作畅玩,无暗病,散热良好,可小刀。',                                                       6899, 9999, 'FAIR',      'B', 'ON_SALE', 174, 15, null, '0', 103, 1, now() - interval '7 day'),
(1, 41, 12, '索尼 A7M4 全画幅微单 单机身',        '快门 1.2 万次,成色良好,传感器无坏点,附两块原装电池与存储卡。',                                      10999, 16999, 'GOOD',   'A', 'ON_SALE', 231, 40, null, '0', 103, 1, now() - interval '4 day'),
(1, 42, 13, '佳能 5D4 单反 24-70 f2.8 套机',      '影楼退役机,快门 8 万次,功能一切正常,镜头无霉无雾,适合学习摄影。',                                  8999, 18999, 'FAIR',   'C', 'ON_SALE', 88,  6,  null, '0', 103, 1, now() - interval '8 day'),
(1, 51, 1,  'AirPods Pro 2 代 充电盒版',          '降噪效果一流,耳机本体成色新,充电盒轻微划痕,已消毒清洁。',                                            1199, 1899, 'GOOD',      'A', 'ON_SALE', 320, 58, null, '0', 103, 1, now() - interval '1 day'),
(1, 52, 16, 'Bose QC45 头戴降噪耳机',             '通勤神器,降噪给力,头梁皮无破损,耳罩已换新,佩戴舒适。',                                               899,  2499, 'FAIR',      'B', 'ON_SALE', 67,  5,  null, '0', 103, 1, now() - interval '9 day'),
(1, 61, 18, '任天堂 Switch OLED 日版 白色',       '吃灰出,含底座与手柄,屏幕完美,机身无划痕,附两个实体卡带。',                                          1599, 2599, 'GOOD',      'A', 'ON_SALE', 289, 51, null, '0', 103, 1, now() - interval '3 day'),
(1, 71, 1,  'Apple Watch S9 45mm GPS 版',         '功能正常,表盘轻微使用痕迹,表带全新未用,电池一天一充正常。',                                        1899, 2999, 'GOOD',      'A', 'ON_SALE', 76,  9,  null, '0', 103, 1, now() - interval '5 day'),
(1, 72, 3,  '小米手环 8 NFC 版 黑色',             '轻量运动伴侣,续航 10 天以上,屏幕无划痕,附两条全新表带。',                                            159,  349,  'EXCELLENT', 'S', 'ON_SALE', 134, 11, null, '0', 103, 1, now() - interval '2 day'),
(1, 81, 19, '罗技 MX Master 3S 无线鼠标',         '办公旗舰鼠标,手感极佳,功能正常,外观 9 成新。',                                                         399,  699,  'GOOD',      'A', 'ON_SALE', 58,  4,  null, '0', 103, 1, now() - interval '10 day'),
(1, 82, 20, '安克 65W 氮化镓充电器 + 数据线',     '出差旅行必备,双口输出,全新未拆封,买多了出两个。',                                                     89,   169,  'EXCELLENT', 'S', 'ON_SALE', 102, 7,  null, '0', 103, 1, now() - interval '1 day');

-- ============ 商品图片(演示用占位图) ============
insert into sm_product_image (product_id, url, sort, is_main, create_time)
select id, 'https://picsum.photos/seed/sm' || id || '/600/600', 0, '1', now()
from sm_product where status = 'ON_SALE';

-- ============ 序列归位(避免后续插入主键冲突) ============
select setval(pg_get_serial_sequence('sm_category', 'id'), (select max(id) from sm_category));
select setval(pg_get_serial_sequence('sm_brand', 'id'), (select max(id) from sm_brand));
select setval(pg_get_serial_sequence('sm_category_brand', 'id'), (select max(id) from sm_category_brand));
select setval(pg_get_serial_sequence('sm_product', 'id'), (select max(id) from sm_product));
select setval(pg_get_serial_sequence('sm_product_image', 'id'), (select max(id) from sm_product_image));
