package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.dto.CreateOrderDTO;
import com.example.demo.dto.PayOrderDTO;
import com.example.demo.entity.OrderStatus;
import com.example.demo.service.OrderService;
import com.example.demo.vo.OrderVo;
import com.example.demo.vo.PageVo;
import com.example.demo.vo.PaymentVo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * 当前用户订单接口。
 * 路径位于 /api/orders/** 下，创建订单需要有效登录状态。
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    // ==================== 接口定义 ====================

    /**
     * 查询并返回当前模块所需的数据。
     */
    @GetMapping
    public Result<PageVo<OrderVo>> listOrders(
            @RequestAttribute("userId") Long userId,
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(orderService.listUserOrders(userId, status, page, size));
    }

    /**
     * 查询并返回当前模块所需的数据。
     */
    @GetMapping("/{orderId}")
    public Result<OrderVo> getOrder(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long orderId) {
        return Result.success(orderService.getUserOrder(userId, orderId));
    }

    /** 使用已选购物车商品和指定收货地址创建待支付订单。 */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Result<OrderVo> createOrder(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody CreateOrderDTO dto) {
        return Result.success(orderService.createOrder(userId, dto));
    }

    /** 取消待支付订单，并退回下单时扣减的库存。 */
    @PutMapping("/{orderId}/cancel")
    public Result<OrderVo> cancelOrder(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long orderId) {
        return Result.success(orderService.cancelOrder(userId, orderId));
    }

    /** 模拟支付成功，生成支付记录并将订单推进到待发货。 */
    @PostMapping("/{orderId}/payment")
    @ResponseStatus(HttpStatus.CREATED)
    public Result<PaymentVo> payOrder(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long orderId,
            @Valid @RequestBody PayOrderDTO dto) {
        return Result.success(orderService.payOrder(userId, orderId, dto));
    }

    /** 确认收货，将已发货订单推进到已完成。 */
    @PutMapping("/{orderId}/confirm-receipt")
    public Result<OrderVo> confirmReceipt(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long orderId) {
        return Result.success(orderService.completeOrder(userId, orderId));
    }
}
