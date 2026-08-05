package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.dto.SavePublisherDTO;
import com.example.demo.service.PublisherService;
import com.example.demo.vo.PageVo;
import com.example.demo.vo.PublisherVo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** 出版社管理端接口，仅管理员可访问。 */
@RestController
@RequestMapping("/api/admin/publishers")
public class AdminPublisherController {

    @Autowired
    private PublisherService publisherService;

    @GetMapping
    public Result<PageVo<PublisherVo>> listPublishers(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(publisherService.listPublishers(keyword, page, size));
    }

    @GetMapping("/{publisherId}")
    public Result<PublisherVo> getPublisher(@PathVariable Long publisherId) {
        return Result.success(publisherService.getPublisher(publisherId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Result<PublisherVo> createPublisher(
            @Valid @RequestBody SavePublisherDTO dto) {
        return Result.success(publisherService.createPublisher(dto));
    }

    @PutMapping("/{publisherId}")
    public Result<PublisherVo> updatePublisher(
            @PathVariable Long publisherId,
            @Valid @RequestBody SavePublisherDTO dto) {
        return Result.success(publisherService.updatePublisher(publisherId, dto));
    }

    @DeleteMapping("/{publisherId}")
    public Result<Void> deletePublisher(@PathVariable Long publisherId) {
        publisherService.deletePublisher(publisherId);
        return Result.success(null);
    }
}
