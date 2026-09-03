package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.dto.UserStatusDTO;
import com.example.demo.entity.Role;
import com.example.demo.service.AdminUserService;
import com.example.demo.vo.AdminUserVo;
import com.example.demo.vo.PageVo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 管理端用户管理接口，仅 ADMIN 可访问。 */
@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    @Autowired
    private AdminUserService adminUserService;

    // ==================== 接口定义 ====================

    /**
     * 查询并返回当前模块所需的数据。
     */
    @GetMapping
    public Result<PageVo<AdminUserVo>> listUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Role role,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(adminUserService.listUsers(keyword, status, role, page, size));
    }

    /**
     * 查询并返回当前模块所需的数据。
     */
    @GetMapping("/{userId}")
    public Result<AdminUserVo> getUser(@PathVariable Long userId) {
        return Result.success(adminUserService.getUser(userId));
    }

    /**
     * 校验请求参数并更新当前业务状态或数据。
     */
    @PutMapping("/{userId}/status")
    public Result<AdminUserVo> changeStatus(
            @RequestAttribute("userId") Long operatorId,
            @PathVariable Long userId,
            @Valid @RequestBody UserStatusDTO dto) {
        return Result.success(
                adminUserService.changeStatus(operatorId, userId, dto.getStatus()));
    }
}
