package com.sm.generator.mapper;

import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import com.sm.common.mybatis.core.mapper.BaseMapperPlus;
import com.sm.generator.domain.GenTableColumn;

/**
 * 业务字段 数据层
 *
 * @author Lion Li
 */
@InterceptorIgnore(dataPermission = "true", tenantLine = "true")
public interface GenTableColumnMapper extends BaseMapperPlus<GenTableColumn, GenTableColumn> {

}
