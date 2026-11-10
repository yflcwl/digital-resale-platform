package com.sm.trade.service;

import com.sm.trade.domain.UserAddress;

import java.util.List;

/**
 * 收货地址Service接口
 *
 * @author sm
 */
public interface IUserAddressService {

    /**
     * 我的地址列表
     */
    List<UserAddress> selectMyList();

    /**
     * 新增地址(首个地址自动设为默认)
     */
    Long add(UserAddress address);

    /**
     * 修改地址
     */
    void edit(UserAddress address);

    /**
     * 删除地址
     */
    void remove(Long id);

    /**
     * 设置默认地址
     */
    void setDefault(Long id);

    /**
     * 查询地址(校验归属)
     */
    UserAddress selectById(Long id);
}
