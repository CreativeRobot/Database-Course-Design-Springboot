package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.dto.CreateReviewDTO;
import com.example.demo.dto.UpdateReviewDTO;
import com.example.demo.service.ReviewService;
import com.example.demo.vo.ReviewSummaryVo;
import com.example.demo.vo.ReviewVo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** 图书评价接口，图书评价列表公开，写操作和个人评价查询需要登录。 */
@RestController
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    /** 分页查询图书评价、平均分和评价数量。 */
    @GetMapping("/api/books/{bookId}/reviews")
    public Result<ReviewSummaryVo> listBookReviews(
            @PathVariable Long bookId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(reviewService.listBookReviews(bookId, page, size));
    }

    /** 查询当前用户的评价记录。 */
    @GetMapping("/api/reviews/me")
    public Result<List<ReviewVo>> listMyReviews(
            @RequestAttribute("userId") Long userId) {
        return Result.success(reviewService.listMyReviews(userId));
    }

    /** 对已完成订单中的商品提交评价。 */
    @PostMapping("/api/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    public Result<ReviewVo> createReview(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody CreateReviewDTO dto) {
        return Result.success(reviewService.createReview(userId, dto));
    }

    /** 修改当前用户已有的评价。 */
    @PutMapping("/api/reviews/{reviewId}")
    public Result<ReviewVo> updateReview(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long reviewId,
            @Valid @RequestBody UpdateReviewDTO dto) {
        return Result.success(reviewService.updateReview(userId, reviewId, dto));
    }
}
