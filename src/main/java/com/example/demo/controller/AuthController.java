package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.dto.LoginDTO;
import com.example.demo.dto.RegisterDTO;
import com.example.demo.dto.ForgotPasswordDTO;
import com.example.demo.vo.SecurityQuestionVo;
import com.example.demo.service.AuthService;
import com.example.demo.vo.LoginVo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

/**
 * AuthController REST 控制器，负责接收请求、调用业务服务并返回统一响应。
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthService authService;

    // ==================== 接口定义 ====================

    /**
     * 执行当前模块的辅助处理逻辑。
     */
    @PostMapping("/login")
    public Result<LoginVo> login(@Valid @RequestBody LoginDTO loginDTO) {
        return Result.success(authService.login(loginDTO));
    }

    /**
     * 创建并保存当前业务数据。
     */
    @PostMapping("/register")
    public Result<LoginVo> register(@Valid @RequestBody RegisterDTO registerDTO) {
        return Result.success(authService.register(registerDTO));
    }

    @GetMapping("/security-questions")
    public Result<List<SecurityQuestionVo>> securityQuestions(@RequestParam String username) {
        return Result.success(authService.securityQuestions(username));
    }

    @PostMapping("/forgot-password")
    public Result<Void> forgotPassword(@Valid @RequestBody ForgotPasswordDTO dto) {
        authService.forgotPassword(dto);
        return Result.success(null);
    }
}
