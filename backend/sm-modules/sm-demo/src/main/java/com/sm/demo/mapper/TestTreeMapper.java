package com.sm.demo.mapper;

import com.sm.common.mybatis.annotation.DataColumn;
import com.sm.common.mybatis.annotation.DataPermission;
import com.sm.common.mybatis.core.mapper.BaseMapperPlus;
import com.sm.demo.domain.TestTree;
import com.sm.demo.domain.vo.TestTreeVo;

/**
 * 测试树表Mapper接口
 *
 * @author Lion Li
 * @date 2021-07-26
 */
@DataPermission({
    @DataColumn(key = "deptName", value = "dept_id"),
    @DataColumn(key = "userName", value = "user_id")
})
public interface TestTreeMapper extends BaseMapperPlus<TestTree, TestTreeVo> {

}
