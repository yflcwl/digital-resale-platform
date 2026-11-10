-- ----------------------------
-- 订单状态字典(可在 系统管理 -> 字典管理 中维护)
-- 说明:后端状态机仍以 OrderStatus 常量为准(迁移守卫依赖固定状态码),
--       字典仅用于前端展示与筛选选项,可调整标签名/排序/颜色。
-- ----------------------------

insert into sys_dict_type (dict_id, tenant_id, dict_name, dict_type, create_dept, create_by, create_time, remark)
select (select coalesce(max(dict_id),0)+1 from sys_dict_type), '000000', '订单状态', 'order_status', 103, 1, now(), '二手交易订单状态'
where not exists (select 1 from sys_dict_type where dict_type = 'order_status');

insert into sys_dict_data (dict_code, tenant_id, dict_sort, dict_label, dict_value, dict_type, css_class, list_class, is_default, create_dept, create_by, create_time, remark)
select (select coalesce(max(dict_code),0) from sys_dict_data) + v.sort, '000000', v.sort, v.label, v.value, 'order_status', '', v.list_class, 'N', 103, 1, now(), v.remark
from (values
    (1, '待支付', 'PENDING_PAY', 'warning', '下单未支付'),
    (2, '已支付', 'PAID',      'primary', '支付成功待发货'),
    (3, '已发货', 'SHIPPED',   'primary', '卖家已发货'),
    (4, '已收货', 'RECEIVED',  'success', '买家已确认收货'),
    (5, '已评价', 'REVIEWED',  'success', '买家已评价'),
    (6, '已完成', 'FINISHED',  'success', '交易完成'),
    (7, '已取消', 'CANCELED',  'info',    '超时或手动取消')
) as v(sort, label, value, list_class, remark)
where not exists (select 1 from sys_dict_data d where d.dict_type = 'order_status' and d.dict_value = v.value);
