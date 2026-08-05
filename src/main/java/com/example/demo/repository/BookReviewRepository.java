package com.example.demo.repository;

import com.example.demo.entity.BookReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookReviewRepository extends JpaRepository<BookReview, Long> {
    Page<BookReview> findByBook_IdAndStatusOrderByCreateTimeDesc(
            Long bookId,
            Integer status,
            Pageable pageable
    );

    List<BookReview> findByUser_IdOrderByCreateTimeDesc(Long userId);

    Optional<BookReview> findByOrderItem_Id(Long orderItemId);

    Optional<BookReview> findByIdAndUser_Id(Long reviewId, Long userId);

    boolean existsByOrderItem_Id(Long orderItemId);

    long countByBook_IdAndStatus(Long bookId, Integer status);

    @Query("""
            select avg(review.rating)
            from BookReview review
            where review.book.id = :bookId
              and review.status = :status
            """)
    Double findAverageRatingByBookId(
            @Param("bookId") Long bookId,
            @Param("status") Integer status
    );
}
