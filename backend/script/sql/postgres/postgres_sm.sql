-- ----------------------------
-- 二手数码交易平台与智能质检辅助系统 业务表结构(PostgreSQL)
-- 依赖:先执行 postgres_sm_vue_5.X.sql(若依系统表)
-- ----------------------------

-- ----------------------------
-- 1、用户账户模块
-- ----------------------------

-- 用户资料扩展表
create table sm_user_profile (
    user_id        bigint        not null,
    nickname       varchar(50)   not null,
    avatar         varchar(500)  default '',
    bio            varchar(500)  default '',
    seller_rating  numeric(2,1)  default 5.0,
    rating_count   int           default 0,
    deal_count     int           default 0,
    create_time    timestamp,
    update_time    timestamp
);
comment on table  sm_user_profile                     is '用户资料扩展表';
comment on column sm_user_profile.user_id             is '用户ID(关联sys_user.user_id)';
comment on column sm_user_profile.nickname            is '昵称';
comment on column sm_user_profile.avatar              is '头像URL';
comment on column sm_user_profile.bio                 is '个人简介';
comment on column sm_user_profile.seller_rating       is '卖家综合评分(0~5)';
comment on column sm_user_profile.rating_count        is '收到评价次数';
comment on column sm_user_profile.deal_count          is '成交笔数';
comment on column sm_user_profile.create_time         is '创建时间';
comment on column sm_user_profile.update_time         is '更新时间';
alter table sm_user_profile add constraint pk_sm_user_profile primary key (user_id);

-- 收货地址表
create table sm_user_address (
    id           bigserial,
    user_id      bigint        not null,
    receiver     varchar(50)   not null,
    phone        varchar(20)   not null,
    province     varchar(50)   not null,
    city         varchar(50)   not null,
    district     varchar(50)   default '',
    detail       varchar(200)  not null,
    is_default   char(1)       default '0',
    del_flag     char(1)       default '0',
    create_time  timestamp,
    update_time  timestamp
);
comment on table  sm_user_address              is '收货地址表';
comment on column sm_user_address.id           is '主键';
comment on column sm_user_address.user_id      is '用户ID';
comment on column sm_user_address.receiver     is '收货人';
comment on column sm_user_address.phone        is '联系电话';
comment on column sm_user_address.province     is '省';
comment on column sm_user_address.city         is '市';
comment on column sm_user_address.district     is '区/县';
comment on column sm_user_address.detail       is '详细地址';
comment on column sm_user_address.is_default   is '是否默认地址(0否 1是)';
comment on column sm_user_address.del_flag     is '删除标志(0正常 2删除)';
comment on column sm_user_address.create_time  is '创建时间';
comment on column sm_user_address.update_time  is '更新时间';
alter table sm_user_address add constraint pk_sm_user_address primary key (id);
create index idx_address_user on sm_user_address (user_id);

-- 邮箱验证码表
create table sm_email_code (
    id           bigserial,
    email        varchar(100)  not null,
    code         varchar(10)   not null,
    biz_type     varchar(20)   not null,
    expire_time  timestamp     not null,
    used         char(1)       default '0',
    create_time  timestamp
);
comment on table  sm_email_code             is '邮箱验证码表';
comment on column sm_email_code.id          is '主键';
comment on column sm_email_code.email       is '邮箱';
comment on column sm_email_code.code        is '验证码';
comment on column sm_email_code.biz_type    is '业务类型(REGISTER注册 RESET找回密码)';
comment on column sm_email_code.expire_time is '过期时间';
comment on column sm_email_code.used        is '是否已使用(0否 1是)';
comment on column sm_email_code.create_time is '创建时间';
alter table sm_email_code add constraint pk_sm_email_code primary key (id);
create index idx_email_code_email on sm_email_code (email);

-- ----------------------------
-- 2、商品模块
-- ----------------------------

-- 商品分类表(三级)
create table sm_category (
    id           bigserial,
    parent_id    bigint        default 0,
    name         varchar(50)   not null,
    icon         varchar(255)  default '',
    level        smallint      not null,
    sort         int           default 0,
    status       char(1)       default '0',
    del_flag     char(1)       default '0',
    create_dept  bigint,
    create_by    bigint,
    create_time  timestamp,
    update_by    bigint,
    update_time  timestamp,
    remark       varchar(500)
);
comment on table  sm_category             is '商品分类表';
comment on column sm_category.id          is '主键';
comment on column sm_category.parent_id   is '父分类ID(0为顶级)';
comment on column sm_category.name        is '分类名称';
comment on column sm_category.icon        is '图标URL';
comment on column sm_category.level       is '层级(1/2/3)';
comment on column sm_category.sort        is '排序号';
comment on column sm_category.status      is '状态(0启用 1停用)';
comment on column sm_category.del_flag    is '删除标志(0正常 2删除)';
comment on column sm_category.create_by   is '创建者';
comment on column sm_category.create_time is '创建时间';
comment on column sm_category.update_by   is '更新者';
comment on column sm_category.update_time is '更新时间';
comment on column sm_category.remark      is '备注';
alter table sm_category add constraint pk_sm_category primary key (id);
create index idx_category_parent on sm_category (parent_id);

-- 品牌表
create table sm_brand (
    id           bigserial,
    name         varchar(50)   not null,
    logo         varchar(255)  default '',
    status       char(1)       default '0',
    sort         int           default 0,
    del_flag     char(1)       default '0',
    create_dept  bigint,
    create_by    bigint,
    create_time  timestamp,
    update_by    bigint,
    update_time  timestamp,
    remark       varchar(500)
);
comment on table  sm_brand             is '品牌表';
comment on column sm_brand.id          is '主键';
comment on column sm_brand.name        is '品牌名称';
comment on column sm_brand.logo        is 'Logo URL';
comment on column sm_brand.status      is '状态(0启用 1停用)';
comment on column sm_brand.sort        is '排序号';
comment on column sm_brand.del_flag    is '删除标志(0正常 2删除)';
comment on column sm_brand.create_by   is '创建者';
comment on column sm_brand.create_time is '创建时间';
comment on column sm_brand.update_by   is '更新者';
comment on column sm_brand.update_time is '更新时间';
comment on column sm_brand.remark      is '备注';
alter table sm_brand add constraint pk_sm_brand primary key (id);

-- 分类-品牌关联表
create table sm_category_brand (
    id           bigserial,
    category_id  bigint  not null,
    brand_id     bigint  not null
);
comment on table  sm_category_brand             is '分类-品牌关联表';
comment on column sm_category_brand.id          is '主键';
comment on column sm_category_brand.category_id is '分类ID';
comment on column sm_category_brand.brand_id    is '品牌ID';
alter table sm_category_brand add constraint pk_sm_category_brand primary key (id);
alter table sm_category_brand add constraint uk_category_brand unique (category_id, brand_id);

-- 商品主表
create table sm_product (
    id               bigserial,
    seller_id        bigint           not null,
    category_id      bigint           not null,
    brand_id         bigint,
    title            varchar(100)     not null,
    description      text,
    price            numeric(12,2)    not null,
    original_price   numeric(12,2),
    condition_self   varchar(20),
    quality_grade    char(1),
    status           varchar(20)      not null,
    view_count       int              default 0,
    favorite_count   int              default 0,
    latest_report_id bigint,
    del_flag         char(1)          default '0',
    create_dept      bigint,
    create_by        bigint,
    create_time      timestamp,
    update_by        bigint,
    update_time      timestamp,
    remark           varchar(500)
);
comment on table  sm_product                  is '商品主表';
comment on column sm_product.id               is '主键';
comment on column sm_product.seller_id        is '卖家用户ID';
comment on column sm_product.category_id      is '分类ID';
comment on column sm_product.brand_id         is '品牌ID';
comment on column sm_product.title            is '商品标题';
comment on column sm_product.description      is '商品描述';
comment on column sm_product.price            is '售价';
comment on column sm_product.original_price   is '原价';
comment on column sm_product.condition_self   is '新旧程度自评(EXCELLENT 99新 GOOD 95新 FAIR 9成新 POOR 8成及以下)';
comment on column sm_product.quality_grade    is '质检成色等级(S/A/B/C/D)';
comment on column sm_product.status           is '商品状态(DRAFT草稿 WAITING_QC待质检 ON_SALE在售 LOCKED已锁定 SOLD已售出 OFF_SHELF已下架 AUDITING审核中 REJECTED审核拒绝)';
comment on column sm_product.view_count       is '浏览次数';
comment on column sm_product.favorite_count   is '收藏次数';
comment on column sm_product.latest_report_id is '最新有效质检报告ID';
comment on column sm_product.del_flag         is '删除标志(0正常 2删除)';
comment on column sm_product.create_by        is '创建者';
comment on column sm_product.create_time      is '创建时间';
comment on column sm_product.update_by        is '更新者';
comment on column sm_product.update_time      is '更新时间';
comment on column sm_product.remark           is '备注';
alter table sm_product add constraint pk_sm_product primary key (id);
create index idx_product_status_category on sm_product (status, category_id);
create index idx_product_seller on sm_product (seller_id, status);
create index idx_product_title on sm_product (title);

-- 商品图片表
create table sm_product_image (
    id           bigserial,
    product_id   bigint         not null,
    url          varchar(500)   not null,
    sort         int            default 0,
    is_main      char(1)        default '0',
    create_time  timestamp
);
comment on table  sm_product_image              is '商品图片表';
comment on column sm_product_image.id           is '主键';
comment on column sm_product_image.product_id   is '商品ID';
comment on column sm_product_image.url          is '图片URL';
comment on column sm_product_image.sort         is '排序号';
comment on column sm_product_image.is_main      is '是否主图(0否 1是)';
comment on column sm_product_image.create_time  is '创建时间';
alter table sm_product_image add constraint pk_sm_product_image primary key (id);
create index idx_product_image_product on sm_product_image (product_id);

-- 商品收藏表
create table sm_product_favorite (
    id           bigserial,
    user_id      bigint  not null,
    product_id   bigint  not null,
    create_time  timestamp
);
comment on table  sm_product_favorite              is '商品收藏表';
comment on column sm_product_favorite.id           is '主键';
comment on column sm_product_favorite.user_id      is '用户ID';
comment on column sm_product_favorite.product_id   is '商品ID';
comment on column sm_product_favorite.create_time  is '收藏时间';
alter table sm_product_favorite add constraint pk_sm_product_favorite primary key (id);
alter table sm_product_favorite add constraint uk_favorite unique (user_id, product_id);

-- 浏览历史表
create table sm_product_browse (
    id           bigserial,
    user_id      bigint    not null,
    product_id   bigint    not null,
    browse_time  timestamp not null
);
comment on table  sm_product_browse              is '商品浏览历史表';
comment on column sm_product_browse.id           is '主键';
comment on column sm_product_browse.user_id      is '用户ID';
comment on column sm_product_browse.product_id   is '商品ID';
comment on column sm_product_browse.browse_time  is '浏览时间';
alter table sm_product_browse add constraint pk_sm_product_browse primary key (id);
create index idx_browse_user on sm_product_browse (user_id, browse_time);

-- ----------------------------
-- 3、智能质检模块(核心)
-- ----------------------------

-- 质检任务表
create table sm_inspection_task (
    id           bigserial,
    product_id   bigint         not null,
    round_no     int            not null,
    status       varchar(20)    not null,
    photo_urls   jsonb,
    mq_msg_id    varchar(100),
    model_name   varchar(50),
    confidence   numeric(5,2),
    duration_ms  bigint,
    error_msg    varchar(500),
    del_flag     char(1)        default '0',
    create_dept  bigint,
    create_by    bigint,
    create_time  timestamp,
    update_by    bigint,
    update_time  timestamp
);
comment on table  sm_inspection_task              is '质检任务表';
comment on column sm_inspection_task.id           is '主键';
comment on column sm_inspection_task.product_id   is '商品ID';
comment on column sm_inspection_task.round_no     is '质检轮次(从1开始)';
comment on column sm_inspection_task.status       is '任务状态(SUBMITTED已提交 PROCESSING处理中 SUCCESS成功 NEED_RESHOOT需补拍 FAILED失败)';
comment on column sm_inspection_task.photo_urls   is '提交照片URL列表(jsonb)';
comment on column sm_inspection_task.mq_msg_id    is 'RocketMQ消息ID';
comment on column sm_inspection_task.model_name   is '使用的多模态模型标识';
comment on column sm_inspection_task.confidence   is '综合置信度(0~100)';
comment on column sm_inspection_task.duration_ms  is '质检耗时(毫秒)';
comment on column sm_inspection_task.error_msg    is '失败原因';
comment on column sm_inspection_task.del_flag     is '删除标志(0正常 2删除)';
comment on column sm_inspection_task.create_by    is '创建者';
comment on column sm_inspection_task.create_time  is '创建时间';
comment on column sm_inspection_task.update_by    is '更新者';
comment on column sm_inspection_task.update_time  is '更新时间';
alter table sm_inspection_task add constraint pk_sm_inspection_task primary key (id);
create index idx_task_product on sm_inspection_task (product_id, round_no);

-- 质检报告表
create table sm_inspection_report (
    id              bigserial,
    task_id         bigint         not null,
    product_id      bigint         not null,
    quality_grade   char(1)        not null,
    brand_model     varchar(100),
    defect_summary  text,
    price_min       numeric(12,2),
    price_max       numeric(12,2),
    confidence      numeric(5,2),
    disclaimer      varchar(255)   default 'AI 辅助质检，仅供参考',
    status          varchar(20)    not null default 'VALID',
    del_flag        char(1)        default '0',
    create_dept     bigint,
    create_by       bigint,
    create_time     timestamp,
    update_by       bigint,
    update_time     timestamp
);
comment on table  sm_inspection_report                  is '质检报告表';
comment on column sm_inspection_report.id               is '主键';
comment on column sm_inspection_report.task_id          is '质检任务ID';
comment on column sm_inspection_report.product_id       is '商品ID';
comment on column sm_inspection_report.quality_grade    is '成色等级(S/A/B/C/D)';
comment on column sm_inspection_report.brand_model      is '识别机型(品牌+型号+配置)';
comment on column sm_inspection_report.defect_summary   is '缺陷总结';
comment on column sm_inspection_report.price_min        is '建议价格下限';
comment on column sm_inspection_report.price_max        is '建议价格上限';
comment on column sm_inspection_report.confidence       is '置信度(0~100)';
comment on column sm_inspection_report.disclaimer       is '免责声明';
comment on column sm_inspection_report.status           is '报告状态(VALID有效 INVALID失效)';
comment on column sm_inspection_report.del_flag         is '删除标志(0正常 2删除)';
comment on column sm_inspection_report.create_by        is '创建者';
comment on column sm_inspection_report.create_time      is '创建时间';
comment on column sm_inspection_report.update_by        is '更新者';
comment on column sm_inspection_report.update_time      is '更新时间';
alter table sm_inspection_report add constraint pk_sm_inspection_report primary key (id);
alter table sm_inspection_report add constraint uk_report_task unique (task_id);
create index idx_report_product on sm_inspection_report (product_id, status);

-- 缺陷明细表
create table sm_inspection_defect (
    id           bigserial,
    report_id    bigint         not null,
    defect_type  varchar(20)    not null,
    position     varchar(50),
    severity     varchar(10)    not null,
    description  varchar(500),
    image_url    varchar(500),
    sort         int            default 0
);
comment on table  sm_inspection_defect              is '质检缺陷明细表';
comment on column sm_inspection_defect.id           is '主键';
comment on column sm_inspection_defect.report_id    is '报告ID';
comment on column sm_inspection_defect.defect_type  is '缺陷类型(SCRATCH划痕 CRACK裂纹 DENT磕碰 PAINT_LOSS掉漆 WATER_DAMAGE进水痕 OTHER其他)';
comment on column sm_inspection_defect.position     is '缺陷位置(屏幕/边框/后盖等)';
comment on column sm_inspection_defect.severity     is '严重程度(MILD轻微 MODERATE中等 SEVERE严重)';
comment on column sm_inspection_defect.description  is '缺陷描述';
comment on column sm_inspection_defect.image_url    is '缺陷部位截图URL';
comment on column sm_inspection_defect.sort         is '排序号';
alter table sm_inspection_defect add constraint pk_sm_inspection_defect primary key (id);
create index idx_defect_report on sm_inspection_defect (report_id);

-- 质检报告反馈表
create table sm_inspection_feedback (
    id           bigserial,
    report_id    bigint         not null,
    user_id      bigint         not null,
    content      varchar(500)   not null,
    status       varchar(20)    default 'PENDING',
    reply        varchar(500),
    reply_time   timestamp,
    create_time  timestamp
);
comment on table  sm_inspection_feedback              is '质检报告反馈表';
comment on column sm_inspection_feedback.id           is '主键';
comment on column sm_inspection_feedback.report_id    is '报告ID';
comment on column sm_inspection_feedback.user_id      is '反馈用户ID';
comment on column sm_inspection_feedback.content      is '反馈内容';
comment on column sm_inspection_feedback.status       is '处理状态(PENDING待处理 HANDLED已处理)';
comment on column sm_inspection_feedback.reply        is '处理回复';
comment on column sm_inspection_feedback.reply_time   is '回复时间';
comment on column sm_inspection_feedback.create_time  is '反馈时间';
alter table sm_inspection_feedback add constraint pk_sm_inspection_feedback primary key (id);
create index idx_feedback_report on sm_inspection_feedback (report_id);

-- ----------------------------
-- 4、交易订单模块
-- ----------------------------

-- 订单主表
create table sm_order (
    id               bigserial,
    order_no         varchar(40)    not null,
    buyer_id         bigint         not null,
    seller_id        bigint         not null,
    product_id       bigint         not null,
    product_snapshot jsonb          not null,
    price            numeric(12,2)  not null,
    status           varchar(20)    not null,
    address_snapshot jsonb,
    express_company  varchar(50),
    express_no       varchar(50),
    timeout_time     timestamp,
    pay_time         timestamp,
    ship_time        timestamp,
    receive_time     timestamp,
    finish_time      timestamp,
    cancel_time      timestamp,
    del_flag         char(1)        default '0',
    create_dept      bigint,
    create_by        bigint,
    create_time      timestamp,
    update_by        bigint,
    update_time      timestamp,
    remark           varchar(500)
);
comment on table  sm_order                   is '订单主表';
comment on column sm_order.id                is '主键';
comment on column sm_order.order_no          is '订单号';
comment on column sm_order.buyer_id          is '买家用户ID';
comment on column sm_order.seller_id         is '卖家用户ID';
comment on column sm_order.product_id        is '商品ID';
comment on column sm_order.product_snapshot  is '商品快照(title/image/price/description)';
comment on column sm_order.price             is '实付金额';
comment on column sm_order.status            is '订单状态(PENDING_PAY待支付 PAID已支付 SHIPPED已发货 RECEIVED已收货 REVIEWED已评价 FINISHED已完成 CANCELED已取消)';
comment on column sm_order.address_snapshot  is '收货地址快照(receiver/phone/fullAddress)';
comment on column sm_order.express_company   is '快递公司';
comment on column sm_order.express_no        is '快递单号(仅作记录)';
comment on column sm_order.timeout_time      is '超时自动取消时间';
comment on column sm_order.pay_time          is '支付时间';
comment on column sm_order.ship_time         is '发货时间';
comment on column sm_order.receive_time      is '收货时间';
comment on column sm_order.finish_time       is '完成时间';
comment on column sm_order.cancel_time       is '取消时间';
comment on column sm_order.del_flag          is '删除标志(0正常 2删除)';
comment on column sm_order.create_by         is '创建者';
comment on column sm_order.create_time       is '创建时间';
comment on column sm_order.update_by         is '更新者';
comment on column sm_order.update_time       is '更新时间';
comment on column sm_order.remark            is '备注';
alter table sm_order add constraint pk_sm_order primary key (id);
alter table sm_order add constraint uk_order_no unique (order_no);
create index idx_order_buyer on sm_order (buyer_id, status);
create index idx_order_seller on sm_order (seller_id, status);
create index idx_order_timeout on sm_order (status, timeout_time);

-- 订单状态流转日志表
create table sm_order_status_log (
    id             bigserial,
    order_id       bigint       not null,
    from_status    varchar(20),
    to_status      varchar(20)  not null,
    operator_type  varchar(20)  not null,
    operator_id    bigint,
    remark         varchar(255),
    create_time    timestamp    not null
);
comment on table  sm_order_status_log                  is '订单状态流转日志表';
comment on column sm_order_status_log.id               is '主键';
comment on column sm_order_status_log.order_id         is '订单ID';
comment on column sm_order_status_log.from_status      is '原状态';
comment on column sm_order_status_log.to_status        is '新状态';
comment on column sm_order_status_log.operator_type    is '操作方(BUYER买家 SELLER卖家 SYSTEM系统)';
comment on column sm_order_status_log.operator_id      is '操作人ID';
comment on column sm_order_status_log.remark           is '备注(如取消原因)';
comment on column sm_order_status_log.create_time      is '变更时间';
alter table sm_order_status_log add constraint pk_sm_order_status_log primary key (id);
create index idx_status_log_order on sm_order_status_log (order_id);

-- 模拟支付流水表
create table sm_payment_record (
    id           bigserial,
    order_id     bigint          not null,
    pay_no       varchar(40)     not null,
    amount       numeric(12,2)   not null,
    pay_type     varchar(20)     not null,
    status       varchar(20)     not null,
    pay_time     timestamp,
    remark       varchar(255)
);
comment on table  sm_payment_record             is '模拟支付流水表';
comment on column sm_payment_record.id          is '主键';
comment on column sm_payment_record.order_id    is '订单ID';
comment on column sm_payment_record.pay_no      is '支付流水号';
comment on column sm_payment_record.amount      is '支付金额';
comment on column sm_payment_record.pay_type    is '支付方式(ALIPAY支付宝 WECHAT微信)';
comment on column sm_payment_record.status      is '支付状态(SUCCESS成功 FAILED失败)';
comment on column sm_payment_record.pay_time    is '支付时间';
comment on column sm_payment_record.remark      is '备注';
alter table sm_payment_record add constraint pk_sm_payment_record primary key (id);
alter table sm_payment_record add constraint uk_pay_no unique (pay_no);
create index idx_payment_order on sm_payment_record (order_id);

-- 交易评价表
create table sm_order_review (
    id           bigserial,
    order_id     bigint         not null,
    reviewer_id  bigint         not null,
    reviewee_id  bigint         not null,
    rating       smallint       not null,
    content      varchar(500),
    create_time  timestamp
);
comment on table  sm_order_review              is '交易评价表';
comment on column sm_order_review.id           is '主键';
comment on column sm_order_review.order_id     is '订单ID';
comment on column sm_order_review.reviewer_id  is '评价人ID';
comment on column sm_order_review.reviewee_id  is '被评价人ID(卖家)';
comment on column sm_order_review.rating       is '星级(1~5)';
comment on column sm_order_review.content      is '评价内容';
comment on column sm_order_review.create_time  is '评价时间';
alter table sm_order_review add constraint pk_sm_order_review primary key (id);
alter table sm_order_review add constraint uk_review unique (order_id, reviewer_id);

-- ----------------------------
-- 5、站内聊天模块
-- ----------------------------

-- 聊天会话表
create table sm_chat_conversation (
    id             bigserial,
    buyer_id       bigint        not null,
    seller_id      bigint        not null,
    product_id     bigint        not null,
    last_message   varchar(500),
    last_msg_time  timestamp,
    buyer_unread   int           default 0,
    seller_unread  int           default 0,
    status         char(1)       default '0',
    create_time    timestamp,
    update_time    timestamp
);
comment on table  sm_chat_conversation                 is '聊天会话表';
comment on column sm_chat_conversation.id              is '主键';
comment on column sm_chat_conversation.buyer_id        is '买家ID';
comment on column sm_chat_conversation.seller_id       is '卖家ID';
comment on column sm_chat_conversation.product_id      is '商品ID';
comment on column sm_chat_conversation.last_message    is '最后一条消息摘要';
comment on column sm_chat_conversation.last_msg_time   is '最后消息时间';
comment on column sm_chat_conversation.buyer_unread    is '买家未读数';
comment on column sm_chat_conversation.seller_unread   is '卖家未读数';
comment on column sm_chat_conversation.status          is '状态(0正常 1已关闭)';
comment on column sm_chat_conversation.create_time     is '创建时间';
comment on column sm_chat_conversation.update_time     is '更新时间';
alter table sm_chat_conversation add constraint pk_sm_chat_conversation primary key (id);
alter table sm_chat_conversation add constraint uk_conversation unique (buyer_id, seller_id, product_id);

-- 聊天消息表
create table sm_chat_message (
    id               bigserial,
    conversation_id  bigint       not null,
    sender_id        bigint       not null,
    receiver_id      bigint       not null,
    msg_type         varchar(20)  not null,
    content          text,
    product_id       bigint,
    is_read          char(1)      default '0',
    create_time      timestamp    not null
);
comment on table  sm_chat_message                   is '聊天消息表';
comment on column sm_chat_message.id                is '主键';
comment on column sm_chat_message.conversation_id   is '会话ID';
comment on column sm_chat_message.sender_id         is '发送人ID';
comment on column sm_chat_message.receiver_id       is '接收人ID';
comment on column sm_chat_message.msg_type          is '消息类型(TEXT文本 IMAGE图片 PRODUCT_CARD商品卡片 BARGAIN议价)';
comment on column sm_chat_message.content           is '消息内容';
comment on column sm_chat_message.product_id        is '引用商品ID(商品卡片消息)';
comment on column sm_chat_message.is_read           is '是否已读(0否 1是)';
comment on column sm_chat_message.create_time       is '发送时间';
alter table sm_chat_message add constraint pk_sm_chat_message primary key (id);
create index idx_msg_conv_time on sm_chat_message (conversation_id, create_time);

-- ----------------------------
-- 6、消息通知模块
-- ----------------------------

-- 站内通知表
create table sm_notification (
    id           bigserial,
    user_id      bigint         not null,
    type         varchar(20)    not null,
    title        varchar(100)   not null,
    content      varchar(500),
    link         varchar(255),
    is_read      char(1)        default '0',
    read_time    timestamp,
    create_time  timestamp      not null
);
comment on table  sm_notification              is '站内通知表';
comment on column sm_notification.id           is '主键';
comment on column sm_notification.user_id      is '接收人ID';
comment on column sm_notification.type         is '通知类型(ORDER订单 INSPECTION质检 SYSTEM系统)';
comment on column sm_notification.title        is '通知标题';
comment on column sm_notification.content      is '内容摘要';
comment on column sm_notification.link         is '跳转链接';
comment on column sm_notification.is_read      is '是否已读(0否 1是)';
comment on column sm_notification.read_time    is '阅读时间';
comment on column sm_notification.create_time  is '通知时间';
alter table sm_notification add constraint pk_sm_notification primary key (id);
create index idx_notify_user on sm_notification (user_id, is_read);

-- ----------------------------
-- 7、管理后台模块
-- ----------------------------

-- 首页轮播图表
create table sm_carousel (
    id           bigserial,
    title        varchar(100)   not null,
    image_url    varchar(500)   not null,
    link         varchar(255),
    sort         int            default 0,
    status       char(1)        default '0',
    del_flag     char(1)        default '0',
    create_dept  bigint,
    create_by    bigint,
    create_time  timestamp,
    update_by    bigint,
    update_time  timestamp,
    remark       varchar(500)
);
comment on table  sm_carousel             is '首页轮播图表';
comment on column sm_carousel.id          is '主键';
comment on column sm_carousel.title       is '标题';
comment on column sm_carousel.image_url   is '图片URL';
comment on column sm_carousel.link        is '跳转链接';
comment on column sm_carousel.sort        is '排序号';
comment on column sm_carousel.status      is '状态(0展示 1隐藏)';
comment on column sm_carousel.del_flag    is '删除标志(0正常 2删除)';
comment on column sm_carousel.create_by   is '创建者';
comment on column sm_carousel.create_time is '创建时间';
comment on column sm_carousel.update_by   is '更新者';
comment on column sm_carousel.update_time is '更新时间';
comment on column sm_carousel.remark      is '备注';
alter table sm_carousel add constraint pk_sm_carousel primary key (id);

-- 商品举报表
create table sm_report (
    id             bigserial,
    reporter_id    bigint         not null,
    product_id     bigint         not null,
    reason         varchar(50)    not null,
    content        varchar(500),
    status         varchar(20)    default 'PENDING',
    handler_id     bigint,
    handle_remark  varchar(500),
    handle_time    timestamp,
    del_flag       char(1)        default '0',
    create_time    timestamp
);
comment on table  sm_report                  is '商品举报表';
comment on column sm_report.id               is '主键';
comment on column sm_report.reporter_id      is '举报人ID';
comment on column sm_report.product_id       is '被举报商品ID';
comment on column sm_report.reason           is '举报原因(违禁品/售假/虚假描述/其他)';
comment on column sm_report.content          is '举报说明';
comment on column sm_report.status           is '处理状态(PENDING待处理 HANDLED已处理 REJECTED驳回)';
comment on column sm_report.handler_id        is '处理人(管理员)ID';
comment on column sm_report.handle_remark    is '处理备注';
comment on column sm_report.handle_time      is '处理时间';
comment on column sm_report.del_flag         is '删除标志(0正常 2删除)';
comment on column sm_report.create_time      is '举报时间';
alter table sm_report add constraint pk_sm_report primary key (id);
create index idx_report_product_id on sm_report (product_id);
