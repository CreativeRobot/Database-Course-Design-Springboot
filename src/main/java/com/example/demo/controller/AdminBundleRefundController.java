package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.dto.ReviewRefundDTO;
import com.example.demo.entity.RefundStatus;
import com.example.demo.entity.RefundType;
import com.example.demo.service.BundleRefundService;
import com.example.demo.vo.BundleRefundRequestVo;
import com.example.demo.vo.PageVo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/bundle-refunds")
public class AdminBundleRefundController {
    @Autowired private BundleRefundService bundleRefundService;

    @GetMapping
    public Result<PageVo<BundleRefundRequestVo>> list(
            @RequestParam(required = false) RefundStatus status,
            @RequestParam(required = false) RefundType type,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(bundleRefundService.listAdmin(status, type, page, size));
    }

    @GetMapping("/{refundId}")
    public Result<BundleRefundRequestVo> detail(@PathVariable Long refundId) {
        return Result.success(bundleRefundService.getAdmin(refundId));
    }

    @PutMapping("/{refundId}/review")
    public Result<BundleRefundRequestVo> review(
            @PathVariable Long refundId,
            @RequestAttribute("userId") Long adminId,
            @Valid @RequestBody ReviewRefundDTO dto) {
        return Result.success(bundleRefundService.toVoForController(bundleRefundService.review(refundId, adminId, dto)));
    }
}
