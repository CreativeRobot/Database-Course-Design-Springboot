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

/**
 * AdminRefundController REST 控制器，负责接收请求、调用业务服务并返回统一响应。
 */
@RestController
@RequestMapping("/api/admin/refunds")
public class AdminRefundController {
    @Autowired private RefundService refundService;

    // ==================== 接口定义 ====================

    /**
     * 查询并返回当前模块所需的数据。
     */
    @GetMapping
    public Result<PageVo<RefundRequestVo>> list(
            @RequestParam(required = false) RefundStatus status,
            @RequestParam(required = false) RefundType type,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(refundService.listAdmin(status, type, page, size));
    }

    /**
     * 执行当前模块的业务处理逻辑。
     */
    @GetMapping("/{refundId}")
    public Result<RefundRequestVo> detail(@PathVariable Long refundId) {
        return Result.success(refundService.getAdmin(refundId));
    }

    /**
     * 执行当前模块的业务处理逻辑。
     */
    @PutMapping("/{refundId}/review")
    public Result<RefundRequestVo> review(
            @PathVariable Long refundId,
            @RequestAttribute("userId") Long adminId,
            @Valid @RequestBody ReviewRefundDTO dto) {
        return Result.success(refundService.toVoForController(refundService.review(refundId, adminId, dto)));
    }
}
