package com.example.demo.service;

import com.example.demo.common.exception.BusinessException;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.vo.AdminUserVo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceTests {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AdminUserService adminUserService;

    @Test
    void listsUsersWithProfileFieldsButNeverExposesPassword() {
        User user = User.builder()
                .id(7L)
                .username("alice")
                .password("$2a$secret")
                .nickname("Alice")
                .email("alice@example.com")
                .status(1)
                .role(Role.CUSTOMER)
                .build();
        when(userRepository.searchForAdmin(eq("alice"), eq(1), eq(Role.CUSTOMER), any()))
                .thenReturn(new PageImpl<>(List.of(user), PageRequest.of(0, 20), 1));

        var result = adminUserService.listUsers(" alice ", 1, Role.CUSTOMER, 1, 20);

        assertEquals(1, result.getTotal());
        AdminUserVo vo = result.getRecords().get(0);
        assertEquals("alice", vo.getUsername());
        assertEquals("Alice", vo.getNickname());
        // AdminUserVo deliberately has no password field.
        verify(userRepository).searchForAdmin(eq("alice"), eq(1), eq(Role.CUSTOMER), any());
    }

    @Test
    void refusesToDisableTheCurrentAdmin() {

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> adminUserService.changeStatus(1L, 1L, 0));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void refusesToDisableTheLastActiveAdmin() {
        User admin = User.builder().id(2L).username("other-admin").status(1).role(Role.ADMIN).build();
        when(userRepository.findById(2L)).thenReturn(Optional.of(admin));
        when(userRepository.countByRoleAndStatus(Role.ADMIN, 1)).thenReturn(1L);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> adminUserService.changeStatus(1L, 2L, 0));

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void changesCustomerStatusAndReturnsUpdatedProfile() {
        User user = User.builder().id(3L).username("customer").status(1).role(Role.CUSTOMER).build();
        when(userRepository.findById(3L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AdminUserVo result = adminUserService.changeStatus(1L, 3L, 0);

        assertEquals(0, user.getStatus());
        assertEquals(0, result.getStatus());
        verify(userRepository).save(user);
    }
}


