package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.service.AdminStatisticsService;
import com.example.demo.vo.AdminStatisticsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * AdminStatisticsController REST 控制器，负责接收请求、调用业务服务并返回统一响应。
 */
@RestController
@RequestMapping("/api/admin/statistics")
public class AdminStatisticsController {

    @Autowired
    private AdminStatisticsService adminStatisticsService;

    // ==================== 接口定义 ====================

    /**
     * 执行当前模块的业务处理逻辑。
     */
    @GetMapping("/overview")
    public Result<AdminStatisticsVo> overview(
            @RequestParam(defaultValue = "6") int months,
            @RequestParam(defaultValue = "10") int top,
            @RequestParam(defaultValue = "5") int lowStockThreshold) {
        return Result.success(
                adminStatisticsService.getOverview(months, top, lowStockThreshold));
    }
}
