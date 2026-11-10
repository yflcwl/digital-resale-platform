-- ----------------------------
-- 品牌重构:由独立表迁移为字典管理(product_brand)
-- 1) 商品新增 brand_name 字段并迁移数据
-- 2) 现有品牌写入字典(系统管理 -> 字典管理 可维护)
-- 3) 移除"平台管理 -> 品牌管理"菜单(由字典管理替代)
-- 说明:sm_brand / sm_category_brand 表保留但不再使用,便于回滚
-- ----------------------------

-- 1) 商品表新增品牌名称字段
alter table sm_product add column if not exists brand_name varchar(50);

update sm_product p
set brand_name = b.name
from sm_brand b
where p.brand_id = b.id
  and (p.brand_name is null or p.brand_name = '');

-- 2) 品牌字典类型
insert into sys_dict_type (dict_id, tenant_id, dict_name, dict_type, create_dept, create_by, create_time, remark)
select (select coalesce(max(dict_id),0)+1 from sys_dict_type), '000000', '商品品牌', 'product_brand', 103, 1, now(), '二手商品品牌(原品牌表迁移)'
where not exists (select 1 from sys_dict_type where dict_type = 'product_brand');

-- 2.1) 品牌字典数据(编号用窗口函数生成,避免逐行子查询同值)
insert into sys_dict_data (dict_code, tenant_id, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, create_dept, create_by, create_time, remark)
select
    (select coalesce(max(dict_code), 0) from sys_dict_data)
        + row_number() over (order by b.sort),
    '000000',
    b.sort,
    b.name,
    b.name,
    'product_brand',
    '',
    '',
    'N',
    103,
    1,
    now(),
    '由品牌表迁移'
from sm_brand b
where b.del_flag = '0'
  and not exists (select 1 from sys_dict_data d where d.dict_type = 'product_brand' and d.dict_value = b.name);

-- 3) 移除品牌管理菜单(改为字典管理维护)
delete from sys_role_menu where menu_id = 2024;
delete from sys_menu where menu_id = 2024;
