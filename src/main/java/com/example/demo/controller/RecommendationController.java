package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.service.RecommendationService;
import com.example.demo.vo.RecommendationHomeVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * RecommendationController REST 控制器，负责接收请求、调用业务服务并返回统一响应。
 */
@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    @Autowired
    private RecommendationService recommendationService;

    // ==================== 接口定义 ====================

    /**
     * 执行当前模块的业务处理逻辑。
     */
    @GetMapping("/home")
    public Result<RecommendationHomeVo> home(
            @RequestAttribute(value = "userId", required = false) Long userId,
            @RequestParam(defaultValue = "12") int limit,
            @RequestParam(defaultValue = "1") int page) {
        return Result.success(recommendationService.getHomeRecommendations(userId, limit, page));
    }
}


