package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.entity.OrderStatus;
import com.example.demo.service.OrderService;
import com.example.demo.vo.OrderVo;
import com.example.demo.vo.PageVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 订单管理端接口。
 * 路径位于 /api/admin/** 下，仅管理员可以执行发货操作。
 */
@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping
    public Result<PageVo<OrderVo>> listOrders(
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(
                orderService.listAdminOrders(orderNo, userId, status, page, size));
    }

    @GetMapping("/{orderId}")
    public Result<OrderVo> getOrder(@PathVariable Long orderId) {
        return Result.success(orderService.getAdminOrder(orderId));
    }

    /** 将待发货订单推进到已发货状态。 */
    @PutMapping("/{orderId}/ship")
    public Result<OrderVo> shipOrder(@PathVariable Long orderId) {
        return Result.success(orderService.shipOrder(orderId));
    }
}
