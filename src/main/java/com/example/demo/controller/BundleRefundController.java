package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.dto.CreateBundleRefundRequestDTO;
import com.example.demo.entity.BundleRefundRequest;
import com.example.demo.service.BundleRefundService;
import com.example.demo.vo.BundleRefundRequestVo;
import com.example.demo.vo.PageVo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class BundleRefundController {
    @Autowired private BundleRefundService bundleRefundService;

    @PostMapping("/{orderId}/bundle-refunds")
    @ResponseStatus(HttpStatus.CREATED)
    public Result<BundleRefundRequestVo> create(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long orderId,
            @Valid @RequestBody CreateBundleRefundRequestDTO dto) {
        BundleRefundRequest request = bundleRefundService.createRequest(userId, orderId, dto);
        return Result.success(bundleRefundService.toVoForController(request));
    }

    @GetMapping("/bundle-refunds")
    public Result<PageVo<BundleRefundRequestVo>> list(
            @RequestAttribute("userId") Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(bundleRefundService.listUser(userId, page, size));
    }

    @GetMapping("/bundle-refunds/{refundId}")
    public Result<BundleRefundRequestVo> detail(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long refundId) {
        return Result.success(bundleRefundService.getUser(userId, refundId));
    }
}
