package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
public class TestController {
    /**
     * 仅作为测试是否链接使用
     */
    @GetMapping("/hello")
    public String hello() {
        return "Hello World!";
    }
}
