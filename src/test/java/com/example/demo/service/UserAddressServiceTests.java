package com.example.demo.service;

import com.example.demo.dto.SaveUserAddressDTO;
import com.example.demo.entity.User;
import com.example.demo.entity.UserAddress;
import com.example.demo.repository.UserAddressRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.vo.UserAddressVo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAddressServiceTests {

    @Mock
    private UserAddressRepository userAddressRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserAddressService userAddressService;

    @Test
    void createAddressLocksUserBeforeChangingDefaultAddress() {
        User user = User.builder().id(1L).status(1).build();
        SaveUserAddressDTO dto = new SaveUserAddressDTO();
        dto.setReceiverName("张三");
        dto.setReceiverPhone("13800000000");
        dto.setProvince("广东省");
        dto.setCity("深圳市");
        dto.setDetailAddress("科技园1号");
        dto.setDefaultAddress(true);

        when(userRepository.findByIdAndStatusForUpdate(1L, 1)).thenReturn(Optional.of(user));
        when(userAddressRepository.countByUser_Id(1L)).thenReturn(1L);
        when(userAddressRepository.save(any(UserAddress.class)))
                .thenAnswer(invocation -> {
                    UserAddress address = invocation.getArgument(0);
                    address.setId(20L);
                    return address;
                });

        UserAddressVo result = userAddressService.createAddress(1L, dto);

        assertTrue(result.getDefaultAddress());
        verify(userRepository).findByIdAndStatusForUpdate(1L, 1);
        verify(userAddressRepository).clearDefaultAddress(1L);
    }
}
