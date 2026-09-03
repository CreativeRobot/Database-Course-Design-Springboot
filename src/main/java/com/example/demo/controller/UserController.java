package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.dto.ChangePasswordDTO;
import com.example.demo.dto.UpdateSecurityQuestionsDTO;
import com.example.demo.dto.UpdateProfileDTO;
import com.example.demo.service.UserService;
import com.example.demo.vo.UserProfileVo;
import com.example.demo.vo.SecurityQuestionVo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import org.springframework.web.multipart.MultipartFile;

/**
 * UserController REST 控制器，负责接收请求、调用业务服务并返回统一响应。
 */
@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private UserService userService;

    // ==================== 接口定义 ====================

    /**
     * 查询并返回当前模块所需的数据。
     */
    @GetMapping("/me")
    public Result<UserProfileVo> getCurrentUser(@RequestAttribute("userId") Long userId) {
        return Result.success(userService.getProfile(userId));
    }

    /**
     * 校验请求参数并更新当前业务状态或数据。
     */
    @PutMapping("/me")
    public Result<UserProfileVo> updateCurrentUser(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody UpdateProfileDTO updateProfileDTO) {
        return Result.success(userService.updateProfile(userId, updateProfileDTO));
    }

    /**
     * 校验请求参数并更新当前业务状态或数据。
     */
    @PostMapping(value = "/me/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<UserProfileVo> updateAvatar(
            @RequestAttribute("userId") Long userId,
            @RequestParam("file") MultipartFile file) {
        return Result.success(userService.updateAvatar(userId, file));
    }

    /**
     * 校验请求参数并更新当前业务状态或数据。
     */
    @PutMapping("/me/password")
    public Result<Void> changePassword(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody ChangePasswordDTO changePasswordDTO) {
        userService.changePassword(userId, changePasswordDTO);
        return Result.success(null);
    }

    @GetMapping("/me/security-questions")
    public Result<List<SecurityQuestionVo>> securityQuestions(@RequestAttribute("userId") Long userId) {
        return Result.success(userService.getSecurityQuestions(userId));
    }

    @PutMapping("/me/security-questions")
    public Result<List<SecurityQuestionVo>> updateSecurityQuestions(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody UpdateSecurityQuestionsDTO dto) {
        return Result.success(userService.updateSecurityQuestions(userId, dto));
    }
}
