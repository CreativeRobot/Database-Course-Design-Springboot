package com.example.demo.repository;

import com.example.demo.entity.BookOrder;
import com.example.demo.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;

public interface BookOrderRepository extends JpaRepository<BookOrder, Long> {
    Optional<BookOrder> findByOrderNo(String orderNo);

    boolean existsByOrderNo(String orderNo);

    Page<BookOrder> findByUser_IdOrderByCreateTimeDesc(Long userId, Pageable pageable);

    Page<BookOrder> findByUser_IdAndStatusOrderByCreateTimeDesc(
            Long userId,
            OrderStatus status,
            Pageable pageable
    );

    Page<BookOrder> findByUser_IdAndStatusInOrderByCreateTimeDesc(
            Long userId,
            Collection<OrderStatus> statuses,
            Pageable pageable
    );

    Page<BookOrder> findByStatusOrderByCreateTimeAsc(
            OrderStatus status,
            Pageable pageable
    );
}