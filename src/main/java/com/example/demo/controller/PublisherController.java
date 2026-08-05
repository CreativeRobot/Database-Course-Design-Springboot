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

@RestController
@RequestMapping("/api/publishers")
public class PublisherController {

    @Autowired
    private PublisherService publisherService;

    @GetMapping
    public Result<PageVo<PublisherVo>> listPublishers(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(publisherService.listPublishers(keyword, page, size));
    }
}
