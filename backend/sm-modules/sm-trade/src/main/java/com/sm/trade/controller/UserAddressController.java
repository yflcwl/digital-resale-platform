package com.sm.trade.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.sm.common.core.domain.R;
import com.sm.trade.domain.UserAddress;
import com.sm.trade.service.IUserAddressService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 收货地址Controller(用户端)
 *
 * @author sm
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/trade/address")
public class UserAddressController {

    private final IUserAddressService addressService;

    /**
     * 我的地址列表
     */
    @SaCheckLogin
    @GetMapping("/list")
    public R<List<UserAddress>> list() {
        return R.ok(addressService.selectMyList());
    }

    /**
     * 新增地址
     */
    @SaCheckLogin
    @PostMapping
    public R<Long> add(@RequestBody UserAddress address) {
        return R.ok(addressService.add(address));
    }

    /**
     * 修改地址
     */
    @SaCheckLogin
    @PutMapping
    public R<Void> edit(@RequestBody UserAddress address) {
        addressService.edit(address);
        return R.ok();
    }

    /**
     * 删除地址
     */
    @SaCheckLogin
    @DeleteMapping("/{id}")
    public R<Void> remove(@PathVariable @NotNull(message = "地址ID不能为空") Long id) {
        addressService.remove(id);
        return R.ok();
    }

    /**
     * 设为默认
     */
    @SaCheckLogin
    @PostMapping("/{id}/default")
    public R<Void> setDefault(@PathVariable @NotNull(message = "地址ID不能为空") Long id) {
        addressService.setDefault(id);
        return R.ok();
    }
}
