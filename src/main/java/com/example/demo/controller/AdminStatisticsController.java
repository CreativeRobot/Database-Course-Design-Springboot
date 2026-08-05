package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.service.AdminStatisticsService;
import com.example.demo.vo.AdminStatisticsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/statistics")
public class AdminStatisticsController {

    @Autowired
    private AdminStatisticsService adminStatisticsService;

    @GetMapping("/overview")
    public Result<AdminStatisticsVo> overview(
            @RequestParam(defaultValue = "6") int months,
            @RequestParam(defaultValue = "10") int top,
            @RequestParam(defaultValue = "5") int lowStockThreshold) {
        return Result.success(
                adminStatisticsService.getOverview(months, top, lowStockThreshold));
    }
}
