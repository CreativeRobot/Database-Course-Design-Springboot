package com.example.demo.repository;

import com.example.demo.entity.OrderItem;
import com.example.demo.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrder_IdOrderByIdAsc(Long orderId);

    List<OrderItem> findByBook_IdOrderByIdDesc(Long bookId);

    @Query("""
            select coalesce(sum(orderItem.quantity), 0)
            from OrderItem orderItem
            where orderItem.book.id = :bookId
              and orderItem.order.status in :statuses
            """)
    Long sumSoldQuantityByBookId(
            @Param("bookId") Long bookId,
            @Param("statuses") Collection<OrderStatus> statuses
    );
}