package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.dto.ReviewStatusDTO;
import com.example.demo.service.ReviewService;
import com.example.demo.vo.PageVo;
import com.example.demo.vo.ReviewVo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * AdminReviewController REST 控制器，负责接收请求、调用业务服务并返回统一响应。
 */
@RestController
@RequestMapping("/api/admin/reviews")
public class AdminReviewController {

    @Autowired
    private ReviewService reviewService;

    // ==================== 接口定义 ====================

    /**
     * 查询并返回当前模块所需的数据。
     */
    @GetMapping
    public Result<PageVo<ReviewVo>> listReviews(
            @RequestParam(required = false) Long bookId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(
                reviewService.listAdminReviews(bookId, userId, status, page, size));
    }

    /**
     * 查询并返回当前模块所需的数据。
     */
    @GetMapping("/{reviewId}")
    public Result<ReviewVo> getReview(@PathVariable Long reviewId) {
        return Result.success(reviewService.getAdminReview(reviewId));
    }

    /**
     * 校验请求参数并更新当前业务状态或数据。
     */
    @PutMapping("/{reviewId}/status")
    public Result<ReviewVo> changeStatus(
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewStatusDTO dto) {
        return Result.success(
                reviewService.changeReviewStatus(reviewId, dto.getStatus()));
    }
}
