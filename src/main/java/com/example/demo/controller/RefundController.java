package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.dto.CreateRefundRequestDTO;
import com.example.demo.entity.RefundRequest;
import com.example.demo.service.RefundService;
import com.example.demo.vo.PageVo;
import com.example.demo.vo.RefundRequestVo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

/**
 * RefundController REST 控制器，负责接收请求、调用业务服务并返回统一响应。
 */
@RestController
@RequestMapping("/api/orders")
public class RefundController {
    @Autowired private RefundService refundService;

    // ==================== 接口定义 ====================

    /**
     * 创建并保存当前业务数据。
     */
    @PostMapping("/{orderId}/refunds")
    @ResponseStatus(HttpStatus.CREATED)
    public Result<RefundRequestVo> create(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long orderId,
            @Valid @RequestBody CreateRefundRequestDTO dto) {
        dto.setOrderId(orderId);
        return Result.success(refundService.toVoForController(refundService.createRequest(userId, dto)));
    }

    /**
     * 查询并返回当前模块所需的数据。
     */
    @GetMapping("/refunds")
    public Result<PageVo<RefundRequestVo>> list(
            @RequestAttribute("userId") Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(refundService.listUser(userId, page, size));
    }

    /**
     * 执行当前模块的业务处理逻辑。
     */
    @GetMapping("/refunds/{refundId}")
    public Result<RefundRequestVo> detail(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long refundId) {
        return Result.success(refundService.getUser(userId, refundId));
    }
}
