package com.sm.trade.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.sm.common.core.exception.ServiceException;
import com.sm.common.satoken.utils.LoginHelper;
import com.sm.trade.domain.UserAddress;
import com.sm.trade.mapper.UserAddressMapper;
import com.sm.trade.service.IUserAddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

/**
 * 收货地址Service业务层处理
 *
 * @author sm
 */
@RequiredArgsConstructor
@Service
public class UserAddressServiceImpl implements IUserAddressService {

    private final UserAddressMapper baseMapper;

    @Override
    public List<UserAddress> selectMyList() {
        return baseMapper.selectList(Wrappers.<UserAddress>lambdaQuery()
            .eq(UserAddress::getUserId, LoginHelper.getUserId())
            .orderByDesc(UserAddress::getIsDefault)
            .orderByDesc(UserAddress::getUpdateTime));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long add(UserAddress address) {
        Long userId = LoginHelper.getUserId();
        address.setId(null);
        address.setUserId(userId);
        long count = baseMapper.selectCount(Wrappers.<UserAddress>lambdaQuery()
            .eq(UserAddress::getUserId, userId));
        address.setIsDefault(count == 0 ? "1" : "0");
        address.setCreateTime(new Date());
        address.setUpdateTime(new Date());
        if (baseMapper.insert(address) <= 0) {
            throw new ServiceException("新增地址失败");
        }
        return address.getId();
    }

    @Override
    public void edit(UserAddress address) {
        UserAddress exist = selectById(address.getId());
        checkOwner(exist);
        UserAddress update = new UserAddress();
        update.setId(address.getId());
        update.setReceiver(address.getReceiver());
        update.setPhone(address.getPhone());
        update.setProvince(address.getProvince());
        update.setCity(address.getCity());
        update.setDistrict(address.getDistrict());
        update.setDetail(address.getDetail());
        update.setUpdateTime(new Date());
        baseMapper.updateById(update);
    }

    @Override
    public void remove(Long id) {
        UserAddress exist = selectById(id);
        checkOwner(exist);
        baseMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void setDefault(Long id) {
        UserAddress exist = selectById(id);
        checkOwner(exist);
        Long userId = LoginHelper.getUserId();
        LambdaUpdateWrapper<UserAddress> uw = Wrappers.lambdaUpdate();
        uw.set(UserAddress::getIsDefault, "0");
        uw.eq(UserAddress::getUserId, userId);
        baseMapper.update(null, uw);
        LambdaUpdateWrapper<UserAddress> uw2 = Wrappers.lambdaUpdate();
        uw2.set(UserAddress::getIsDefault, "1");
        uw2.eq(UserAddress::getId, id);
        baseMapper.update(null, uw2);
    }

    @Override
    public UserAddress selectById(Long id) {
        return baseMapper.selectById(id);
    }

    private void checkOwner(UserAddress address) {
        if (address == null) {
            throw new ServiceException("地址不存在");
        }
        if (!LoginHelper.getUserId().equals(address.getUserId())) {
            throw new ServiceException("无权操作他人地址");
        }
    }
}
