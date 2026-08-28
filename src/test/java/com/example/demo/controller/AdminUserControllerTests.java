package com.example.demo.controller;

import com.example.demo.entity.Role;
import com.example.demo.service.AdminUserService;
import com.example.demo.vo.AdminUserVo;
import com.example.demo.vo.PageVo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminUserControllerTests {

    @Mock
    private AdminUserService adminUserService;

    @InjectMocks
    private AdminUserController controller;

    @Test
    void forwardsUserListFilters() {
        PageVo<AdminUserVo> page = new PageVo<>();
        when(adminUserService.listUsers("alice", 1, Role.CUSTOMER, 2, 20)).thenReturn(page);

        controller.listUsers("alice", 1, Role.CUSTOMER, 2, 20);

        verify(adminUserService).listUsers("alice", 1, Role.CUSTOMER, 2, 20);
    }
}
