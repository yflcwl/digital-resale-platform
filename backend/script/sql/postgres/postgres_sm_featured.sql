-- ----------------------------
-- 商品"首页精选"标记 + 演示精选商品
-- ----------------------------

alter table sm_product add column if not exists is_featured char(1) default '0';

comment on column sm_product.is_featured is '是否首页精选(0否 1是)';

-- 默认精选几件成色较好的在售商品(管理员可在后台调整)
update sm_product set is_featured = '1'
where id in (
    select id from sm_product
    where status = 'ON_SALE' and quality_grade in ('S', 'A')
    order by price desc
    limit 4
);
