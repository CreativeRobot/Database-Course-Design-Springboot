package com.example.demo.repository;

import com.example.demo.entity.BookPromotion;
import com.example.demo.entity.BookPromotionStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookPromotionRepository extends JpaRepository<BookPromotion, Long> {
    @Query("select p from BookPromotion p join fetch p.book order by p.startTime desc, p.id desc")
    List<BookPromotion> findAllWithBook();

    @Query("select p from BookPromotion p join fetch p.book where p.status = :status and p.startTime <= :now and p.endTime > :now order by p.startTime asc, p.id asc")
    List<BookPromotion> findActiveWithBook(@Param("status") BookPromotionStatus status, @Param("now") LocalDateTime now);

    @Query("select p from BookPromotion p join fetch p.book where p.book.id = :bookId and p.status = :status and p.startTime <= :now and p.endTime > :now order by p.startTime desc")
    List<BookPromotion> findCurrentByBookId(@Param("bookId") Long bookId, @Param("status") BookPromotionStatus status, @Param("now") LocalDateTime now);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from BookPromotion p join fetch p.book where p.id = :id")
    Optional<BookPromotion> findByIdForUpdate(@Param("id") Long id);

    @Query("select (count(p) > 0) from BookPromotion p where p.book.id = :bookId and (:excludedId is null or p.id <> :excludedId) and p.startTime < :endTime and p.endTime > :startTime")
    boolean existsOverlapping(@Param("bookId") Long bookId, @Param("startTime") LocalDateTime startTime,
                              @Param("endTime") LocalDateTime endTime, @Param("excludedId") Long excludedId);
}
