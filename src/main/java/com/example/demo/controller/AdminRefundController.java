package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.dto.ReviewRefundDTO;
import com.example.demo.entity.RefundStatus;
import com.example.demo.entity.RefundType;
import com.example.demo.service.RefundService;
import com.example.demo.vo.PageVo;
import com.example.demo.vo.RefundRequestVo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/refunds")
public class AdminRefundController {
    @Autowired private RefundService refundService;

    @GetMapping
    public Result<PageVo<RefundRequestVo>> list(
            @RequestParam(required = false) RefundStatus status,
            @RequestParam(required = false) RefundType type,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(refundService.listAdmin(status, type, page, size));
    }

    @GetMapping("/{refundId}")
    public Result<RefundRequestVo> detail(@PathVariable Long refundId) {
        return Result.success(refundService.getAdmin(refundId));
    }

    @PutMapping("/{refundId}/review")
    public Result<RefundRequestVo> review(
            @PathVariable Long refundId,
            @RequestAttribute("userId") Long adminId,
            @Valid @RequestBody ReviewRefundDTO dto) {
        return Result.success(refundService.toVoForController(refundService.review(refundId, adminId, dto)));
    }
}
