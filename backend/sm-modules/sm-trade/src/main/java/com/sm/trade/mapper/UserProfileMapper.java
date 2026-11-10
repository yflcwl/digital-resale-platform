package com.sm.trade.mapper;

import com.sm.common.mybatis.core.mapper.BaseMapperPlus;
import com.sm.trade.domain.UserProfile;
import com.sm.trade.domain.vo.SellerInfoVo;
import org.apache.ibatis.annotations.Param;

/**
 * 用户资料扩展Mapper接口
 *
 * @author sm
 */
public interface UserProfileMapper extends BaseMapperPlus<UserProfile, UserProfile> {

    /**
     * 查询卖家信息(昵称/头像/评分)
     */
    SellerInfoVo selectSellerInfo(@Param("userId") Long userId);
}
