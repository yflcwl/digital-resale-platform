-- ----------------------------
-- 商品新旧程度字典(可在 系统管理 -> 字典管理 中维护)
-- ----------------------------

insert into sys_dict_type (dict_id, tenant_id, dict_name, dict_type, create_dept, create_by, create_time, remark)
select (select coalesce(max(dict_id),0)+1 from sys_dict_type), '000000', '商品新旧程度', 'product_condition', 103, 1, now(), '二手商品新旧程度自评'
where not exists (select 1 from sys_dict_type where dict_type = 'product_condition');

insert into sys_dict_data (dict_code, tenant_id, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, create_dept, create_by, create_time, remark)
select (select coalesce(max(dict_code),0)+1 from sys_dict_data), '000000', 1, '99新', 'EXCELLENT', 'product_condition', '', 'success', 'Y', 103, 1, now(), '几乎无使用痕迹'
where not exists (select 1 from sys_dict_data where dict_type = 'product_condition' and dict_value = 'EXCELLENT');

insert into sys_dict_data (dict_code, tenant_id, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, create_dept, create_by, create_time, remark)
select (select coalesce(max(dict_code),0)+1 from sys_dict_data), '000000', 2, '95新', 'GOOD', 'product_condition', '', 'primary', 'N', 103, 1, now(), '轻微使用痕迹'
where not exists (select 1 from sys_dict_data where dict_type = 'product_condition' and dict_value = 'GOOD');

insert into sys_dict_data (dict_code, tenant_id, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, create_dept, create_by, create_time, remark)
select (select coalesce(max(dict_code),0)+1 from sys_dict_data), '000000', 3, '9成新', 'FAIR', 'product_condition', '', 'warning', 'N', 103, 1, now(), '明显使用痕迹'
where not exists (select 1 from sys_dict_data where dict_type = 'product_condition' and dict_value = 'FAIR');

insert into sys_dict_data (dict_code, tenant_id, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, create_dept, create_by, create_time, remark)
select (select coalesce(max(dict_code),0)+1 from sys_dict_data), '000000', 4, '8成新及以下', 'POOR', 'product_condition', '', 'info', 'N', 103, 1, now(), '较重使用痕迹'
where not exists (select 1 from sys_dict_data where dict_type = 'product_condition' and dict_value = 'POOR');
