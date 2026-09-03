package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.service.CaptchaService;
import com.example.demo.vo.CaptchaVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * CaptchaController REST 控制器，负责接收请求、调用业务服务并返回统一响应。
 */
@RestController
@RequestMapping("/api/auth")
public class CaptchaController {

    private final CaptchaService captchaService;

    public CaptchaController(CaptchaService captchaService) {
        this.captchaService = captchaService;
    }

    // ==================== 接口定义 ====================

    /**
     * 查询并返回当前模块所需的数据。
     */
    @GetMapping("/captcha")
    public Result<CaptchaVo> getCaptcha() {
        return Result.success(captchaService.issue());
    }
}
