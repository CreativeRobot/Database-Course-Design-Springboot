package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.service.PublisherService;
import com.example.demo.vo.PageVo;
import com.example.demo.vo.PublisherVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * PublisherController REST 控制器，负责接收请求、调用业务服务并返回统一响应。
 */
@RestController
@RequestMapping("/api/publishers")
public class PublisherController {

    @Autowired
    private PublisherService publisherService;

    // ==================== 接口定义 ====================

    /**
     * 查询并返回当前模块所需的数据。
     */
    @GetMapping
    public Result<PageVo<PublisherVo>> listPublishers(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(publisherService.listPublishers(keyword, page, size));
    }
}
