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

@RestController
@RequestMapping("/api/recommendations")
public class RecommendationController {

    @Autowired
    private RecommendationService recommendationService;

    @GetMapping("/home")
    public Result<RecommendationHomeVo> home(
            @RequestAttribute("userId") Long userId,
            @RequestParam(defaultValue = "12") int limit) {
        return Result.success(recommendationService.getHomeRecommendations(userId, limit));
    }
}
