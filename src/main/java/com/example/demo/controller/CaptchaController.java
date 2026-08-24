package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.service.CaptchaService;
import com.example.demo.vo.CaptchaVo;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class CaptchaController {

    private final CaptchaService captchaService;

    public CaptchaController(CaptchaService captchaService) {
        this.captchaService = captchaService;
    }

    @GetMapping("/captcha")
    public Result<CaptchaVo> getCaptcha() {
        return Result.success(captchaService.issue());
    }
}
