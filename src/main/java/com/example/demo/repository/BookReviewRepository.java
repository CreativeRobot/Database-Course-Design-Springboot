package com.example.demo.repository;

import com.example.demo.entity.BookReview;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * BookReviewRepository 数据访问接口，负责实体持久化及相关查询。
 */
public interface BookReviewRepository extends JpaRepository<BookReview, Long> {
    Page<BookReview> findByBook_IdAndStatusOrderByCreateTimeDesc(
            Long bookId,
            Integer status,
            Pageable pageable
    );

    List<BookReview> findByUser_IdOrderByCreateTimeDesc(Long userId);

    List<BookReview> findByUser_IdAndStatus(Long userId, Integer status);

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

    @Query("""
            select review.book.id, avg(review.rating)
            from BookReview review
            where review.book.id in :bookIds
              and review.status = :status
            group by review.book.id
            """)
    List<Object[]> findAverageRatingsByBookIds(
            @Param("bookIds") List<Long> bookIds,
            @Param("status") Integer status
    );

    @Query("""
            select review
            from BookReview review
            where (:bookId is null or review.book.id = :bookId)
              and (:userId is null or review.user.id = :userId)
              and (:status is null or review.status = :status)
            order by review.createTime desc, review.id desc
            """)
    Page<BookReview> searchForAdmin(
            @Param("bookId") Long bookId,
            @Param("userId") Long userId,
            @Param("status") Integer status,
            Pageable pageable
    );
}
