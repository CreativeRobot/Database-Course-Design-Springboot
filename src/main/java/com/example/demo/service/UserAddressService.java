package com.example.demo.service;

import com.example.demo.common.exception.BusinessException;
import com.example.demo.dto.SaveUserAddressDTO;
import com.example.demo.entity.User;
import com.example.demo.entity.UserAddress;
import com.example.demo.repository.UserAddressRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.vo.UserAddressVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 当前登录用户的收货地址业务。
 * 首个地址自动成为默认地址；删除默认地址后，从剩余地址中自动补选一个默认地址。
 */
@Service
public class UserAddressService {

    @Autowired
    private UserAddressRepository userAddressRepository;

    @Autowired
    private UserRepository userRepository;

    // ==================== 地址查询 ====================

    /** 查询当前用户的全部地址，默认地址排在最前面。 */
    @Transactional(readOnly = true)
    public List<UserAddressVo> listAddresses(Long userId) {
        getActiveUser(userId);
        return userAddressRepository
                .findByUser_IdOrderByDefaultAddressDescCreateTimeDesc(userId)
                .stream()
                .map(this::toVo)
                .toList();
    }

    /** 查询当前用户的一条地址，不允许读取其他用户的地址。 */
    @Transactional(readOnly = true)
    public UserAddressVo getAddress(Long userId, Long addressId) {
        getActiveUser(userId);
        return toVo(getOwnedAddress(userId, addressId));
    }

    // ==================== 地址写操作 ====================

    /**
     * 新增收货地址。
     * 首个地址强制设为默认；显式传入 defaultAddress=true 时替换原默认地址。
     */
    @Transactional
    public UserAddressVo createAddress(Long userId, SaveUserAddressDTO dto) {
        validateAddress(dto);
        User user = getActiveUserForUpdate(userId);
        boolean shouldBeDefault = userAddressRepository.countByUser_Id(userId) == 0
                || Boolean.TRUE.equals(dto.getDefaultAddress());
        if (shouldBeDefault) {
            userAddressRepository.clearDefaultAddress(userId);
        }

        UserAddress address = new UserAddress();
        address.setUser(user);
        copyAddressFields(dto, address);
        address.setDefaultAddress(shouldBeDefault);
        return toVo(userAddressRepository.save(address));
    }

    /**
     * 完整更新一条收货地址。
     * defaultAddress=true 时将其设为默认；未传或传 false 时保留现有默认地址关系。
     */
    @Transactional
    public UserAddressVo updateAddress(
            Long userId, Long addressId, SaveUserAddressDTO dto) {
        validateAddress(dto);
        getActiveUserForUpdate(userId);
        UserAddress address = getOwnedAddress(userId, addressId);
        copyAddressFields(dto, address);

        if (Boolean.TRUE.equals(dto.getDefaultAddress())) {
            userAddressRepository.clearDefaultAddress(userId);
            address.setDefaultAddress(true);
        } else if (!Boolean.TRUE.equals(address.getDefaultAddress())) {
            address.setDefaultAddress(false);
        }
        return toVo(userAddressRepository.save(address));
    }

    /** 将指定地址设为当前用户唯一的默认地址。 */
    @Transactional
    public UserAddressVo setDefaultAddress(Long userId, Long addressId) {
        getActiveUserForUpdate(userId);
        UserAddress address = getOwnedAddress(userId, addressId);
        userAddressRepository.clearDefaultAddress(userId);
        address.setDefaultAddress(true);
        return toVo(userAddressRepository.save(address));
    }

    /**
     * 删除当前用户的一条地址。
     * 删除的是默认地址时，按现有排序选择一个剩余地址作为新默认地址。
     */
    @Transactional
    public void deleteAddress(Long userId, Long addressId) {
        getActiveUserForUpdate(userId);
        UserAddress address = getOwnedAddress(userId, addressId);
        boolean wasDefault = Boolean.TRUE.equals(address.getDefaultAddress());
        userAddressRepository.delete(address);
        userAddressRepository.flush();

        if (wasDefault) {
            userAddressRepository
                    .findByUser_IdOrderByDefaultAddressDescCreateTimeDesc(userId)
                    .stream()
                    .findFirst()
                    .ifPresent(nextDefault -> {
                        nextDefault.setDefaultAddress(true);
                        userAddressRepository.save(nextDefault);
                    });
        }
    }

    // ==================== 私有辅助方法 ====================

    /** 按地址编号和用户编号联合查询，统一处理越权与不存在场景。 */
    private UserAddress getOwnedAddress(Long userId, Long addressId) {
        if (addressId == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "地址不能为空");
        }
        return userAddressRepository.findByIdAndUser_Id(addressId, userId)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND, "收货地址不存在"));
    }

    /** 校验用户仍然存在且账号处于启用状态。 */
    private User getActiveUser(Long userId) {
        if (userId == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "未登录或Token已失效");
        }
        return userRepository.findByIdAndStatus(userId, 1)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND, "用户不存在或已被禁用"));
    }

    /** 写操作锁定用户行，串行化同一用户的默认地址变更。 */
    private User getActiveUserForUpdate(Long userId) {
        if (userId == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "未登录或Token已失效");
        }
        return userRepository.findByIdAndStatusForUpdate(userId, 1)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND, "用户不存在或已被禁用"));
    }

    /** 对 Service 直接调用场景补充必要字段校验。 */
    private void validateAddress(SaveUserAddressDTO dto) {
        if (dto == null
                || !StringUtils.hasText(dto.getReceiverName())
                || !StringUtils.hasText(dto.getReceiverPhone())
                || !StringUtils.hasText(dto.getProvince())
                || !StringUtils.hasText(dto.getCity())
                || !StringUtils.hasText(dto.getDetailAddress())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "收货地址信息不完整");
        }
    }

    /** 将请求中的地址字段复制到实体，并统一去除首尾空格。 */
    private void copyAddressFields(SaveUserAddressDTO dto, UserAddress address) {
        address.setReceiverName(dto.getReceiverName().trim());
        address.setReceiverPhone(dto.getReceiverPhone().trim());
        address.setProvince(dto.getProvince().trim());
        address.setCity(dto.getCity().trim());
        address.setDistrict(trimToNull(dto.getDistrict()));
        address.setDetailAddress(dto.getDetailAddress().trim());
        address.setPostalCode(trimToNull(dto.getPostalCode()));
    }

    /** 将地址实体转换为不包含用户敏感信息的视图对象。 */
    private UserAddressVo toVo(UserAddress address) {
        UserAddressVo vo = new UserAddressVo();
        vo.setId(address.getId());
        vo.setReceiverName(address.getReceiverName());
        vo.setReceiverPhone(address.getReceiverPhone());
        vo.setProvince(address.getProvince());
        vo.setCity(address.getCity());
        vo.setDistrict(address.getDistrict());
        vo.setDetailAddress(address.getDetailAddress());
        vo.setPostalCode(address.getPostalCode());
        vo.setDefaultAddress(address.getDefaultAddress());
        vo.setCreateTime(address.getCreateTime());
        vo.setUpdateTime(address.getUpdateTime());
        return vo;
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
