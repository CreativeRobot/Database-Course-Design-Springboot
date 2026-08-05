package com.example.demo.service;

import com.example.demo.common.exception.BusinessException;
import com.example.demo.dto.CreateReviewDTO;
import com.example.demo.dto.UpdateReviewDTO;
import com.example.demo.entity.BookReview;
import com.example.demo.entity.OrderItem;
import com.example.demo.entity.OrderStatus;
import com.example.demo.entity.User;
import com.example.demo.repository.BookRepository;
import com.example.demo.repository.BookReviewRepository;
import com.example.demo.repository.OrderItemRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.vo.PageVo;
import com.example.demo.vo.ReviewSummaryVo;
import com.example.demo.vo.ReviewVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 图书评价业务。
 * 评价必须绑定当前用户已完成订单中的明细，一条订单明细最多产生一条评价。
 */
@Service
public class ReviewService {

    private static final int ENABLED_STATUS = 1;
    private static final int MAX_PAGE_SIZE = 100;

    @Autowired
    private BookReviewRepository bookReviewRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private UserRepository userRepository;

    // ==================== 公开与用户查询 ====================

    /** 分页查询一本图书的有效评价，同时返回平均分和评价总数。 */
    @Transactional(readOnly = true)
    public ReviewSummaryVo listBookReviews(Long bookId, int page, int size) {
        if (bookId == null || !bookRepository.existsById(bookId)) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "图书不存在");
        }
        Pageable pageable = buildPageable(page, size);
        Page<ReviewVo> reviews = bookReviewRepository
                .findByBook_IdAndStatusOrderByCreateTimeDesc(
                        bookId, ENABLED_STATUS, pageable)
                .map(this::toVo);

        Double averageRating = bookReviewRepository
                .findAverageRatingByBookId(bookId, ENABLED_STATUS);
        ReviewSummaryVo vo = new ReviewSummaryVo();
        vo.setBookId(bookId);
        vo.setAverageRating(averageRating == null ? 0.0 : averageRating);
        vo.setReviewCount(bookReviewRepository
                .countByBook_IdAndStatus(bookId, ENABLED_STATUS));
        vo.setReviews(PageVo.of(reviews));
        return vo;
    }

    /** 查询当前用户提交过的全部评价。 */
    @Transactional(readOnly = true)
    public List<ReviewVo> listMyReviews(Long userId) {
        getActiveUser(userId);
        return bookReviewRepository.findByUser_IdOrderByCreateTimeDesc(userId)
                .stream()
                .map(this::toVo)
                .toList();
    }

    // ==================== 评价写操作 ====================

    /** 为已完成订单中的一条明细提交评价。 */
    @Transactional
    public ReviewVo createReview(Long userId, CreateReviewDTO dto) {
        validateCreateRequest(dto);
        User user = getActiveUser(userId);
        OrderItem orderItem = orderItemRepository
                .findByIdAndOrder_User_Id(dto.getOrderItemId(), userId)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND, "订单明细不存在"));
        if (orderItem.getOrder().getStatus() != OrderStatus.COMPLETED) {
            throw new BusinessException(HttpStatus.CONFLICT, "订单完成后才能评价");
        }
        if (bookReviewRepository.existsByOrderItem_Id(orderItem.getId())) {
            throw new BusinessException(HttpStatus.CONFLICT, "该订单商品已经评价");
        }

        BookReview review = BookReview.builder()
                .user(user)
                .book(orderItem.getBook())
                .orderItem(orderItem)
                .rating(dto.getRating())
                .content(trimToNull(dto.getContent()))
                .status(ENABLED_STATUS)
                .build();
        return toVo(bookReviewRepository.saveAndFlush(review));
    }

    /** 修改当前用户已有评价的评分和内容。 */
    @Transactional
    public ReviewVo updateReview(Long userId, Long reviewId, UpdateReviewDTO dto) {
        validateUpdateRequest(dto);
        getActiveUser(userId);
        BookReview review = bookReviewRepository.findByIdAndUser_Id(reviewId, userId)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND, "评价不存在"));
        review.setRating(dto.getRating());
        review.setContent(trimToNull(dto.getContent()));
        return toVo(bookReviewRepository.save(review));
    }

    // ==================== 私有辅助方法 ====================

    private Pageable buildPageable(int page, int size) {
        if (page < 1) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "页码必须从1开始");
        }
        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new BusinessException(HttpStatus.BAD_REQUEST,
                    "每页数量必须在1到" + MAX_PAGE_SIZE + "之间");
        }
        return PageRequest.of(page - 1, size);
    }

    private void validateCreateRequest(CreateReviewDTO dto) {
        if (dto == null || dto.getOrderItemId() == null || dto.getRating() == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "订单明细和评分不能为空");
        }
        validateRating(dto.getRating());
    }

    private void validateUpdateRequest(UpdateReviewDTO dto) {
        if (dto == null || dto.getRating() == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "评分不能为空");
        }
        validateRating(dto.getRating());
    }

    private void validateRating(Integer rating) {
        if (rating < 1 || rating > 5) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "评分必须在1到5之间");
        }
    }

    private User getActiveUser(Long userId) {
        if (userId == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "未登录或Token已失效");
        }
        return userRepository.findByIdAndStatus(userId, 1)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND, "用户不存在或已被禁用"));
    }

    private ReviewVo toVo(BookReview review) {
        ReviewVo vo = new ReviewVo();
        vo.setId(review.getId());
        vo.setBookId(review.getBook().getId());
        vo.setBookTitle(review.getOrderItem().getBookTitle());
        vo.setOrderItemId(review.getOrderItem().getId());
        vo.setUserId(review.getUser().getId());
        vo.setReviewerName(StringUtils.hasText(review.getUser().getNickname())
                ? review.getUser().getNickname() : review.getUser().getUsername());
        vo.setRating(review.getRating());
        vo.setContent(review.getContent());
        vo.setStatus(review.getStatus());
        vo.setCreateTime(review.getCreateTime());
        vo.setUpdateTime(review.getUpdateTime());
        return vo;
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
