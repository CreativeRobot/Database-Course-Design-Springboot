package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.dto.SaveUserAddressDTO;
import com.example.demo.service.UserAddressService;
import com.example.demo.vo.UserAddressVo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 当前用户收货地址接口。
 * 路径位于 /api/user/addresses/** 下，所有操作均按 JwtInterceptor 提供的 userId 隔离。
 */
@RestController
@RequestMapping("/api/user/addresses")
public class UserAddressController {

    @Autowired
    private UserAddressService userAddressService;

    /** 查询当前用户的全部收货地址。 */
    @GetMapping
    public Result<List<UserAddressVo>> listAddresses(
            @RequestAttribute("userId") Long userId) {
        return Result.success(userAddressService.listAddresses(userId));
    }

    /** 查询当前用户的一条收货地址。 */
    @GetMapping("/{addressId}")
    public Result<UserAddressVo> getAddress(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long addressId) {
        return Result.success(userAddressService.getAddress(userId, addressId));
    }

    /** 新增收货地址；首个地址会自动成为默认地址。 */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Result<UserAddressVo> createAddress(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody SaveUserAddressDTO dto) {
        return Result.success(userAddressService.createAddress(userId, dto));
    }

    /** 完整更新一条收货地址。 */
    @PutMapping("/{addressId}")
    public Result<UserAddressVo> updateAddress(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long addressId,
            @Valid @RequestBody SaveUserAddressDTO dto) {
        return Result.success(userAddressService.updateAddress(userId, addressId, dto));
    }

    /** 将指定地址设为默认收货地址。 */
    @PutMapping("/{addressId}/default")
    public Result<UserAddressVo> setDefaultAddress(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long addressId) {
        return Result.success(userAddressService.setDefaultAddress(userId, addressId));
    }

    /** 删除一条收货地址。 */
    @DeleteMapping("/{addressId}")
    public Result<Void> deleteAddress(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long addressId) {
        userAddressService.deleteAddress(userId, addressId);
        return Result.success(null);
    }
}
