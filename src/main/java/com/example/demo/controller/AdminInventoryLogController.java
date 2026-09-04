package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.entity.InventoryChangeType;
import com.example.demo.service.InventoryLogService;
import com.example.demo.vo.InventoryLogVo;
import com.example.demo.vo.PageVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * AdminInventoryLogController REST 控制器，负责接收请求、调用业务服务并返回统一响应。
 */
@RestController
@RequestMapping("/api/admin/inventory-logs")
public class AdminInventoryLogController {

    @Autowired
    private InventoryLogService inventoryLogService;

    // ==================== 接口定义 ====================

    /**
     * 查询并返回当前模块所需的数据。
     */
    @GetMapping
    public Result<PageVo<InventoryLogVo>> listLogs(
            @RequestParam(required = false) Long bookId,
            @RequestParam(required = false) String bookName,
            @RequestParam(required = false) Long orderId,
            @RequestParam(required = false) InventoryChangeType changeType,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(inventoryLogService.listLogs(
                bookId, bookName, orderId, changeType, startTime, endTime, page, size));
    }
}
