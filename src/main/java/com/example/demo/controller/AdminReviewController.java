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

@RestController
@RequestMapping("/api/admin/reviews")
public class AdminReviewController {

    @Autowired
    private ReviewService reviewService;

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

    @GetMapping("/{reviewId}")
    public Result<ReviewVo> getReview(@PathVariable Long reviewId) {
        return Result.success(reviewService.getAdminReview(reviewId));
    }

    @PutMapping("/{reviewId}/status")
    public Result<ReviewVo> changeStatus(
            @PathVariable Long reviewId,
            @Valid @RequestBody ReviewStatusDTO dto) {
        return Result.success(
                reviewService.changeReviewStatus(reviewId, dto.getStatus()));
    }
}
