package com.example.demo.repository;

import com.example.demo.entity.BookOrder;
import com.example.demo.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Optional;

public interface BookOrderRepository extends JpaRepository<BookOrder, Long> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select o from BookOrder o where o.id = :orderId")
    Optional<BookOrder> findByIdForUpdate(@Param("orderId") Long orderId);
    Optional<BookOrder> findByOrderNo(String orderNo);

    Optional<BookOrder> findByIdAndUser_Id(Long orderId, Long userId);

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

    java.util.List<BookOrder> findByStatusAndExpireTimeLessThanEqualOrderByExpireTimeAsc(
            OrderStatus status,
            LocalDateTime expireTime
    );

    @Query("""
            select bookOrder
            from BookOrder bookOrder
            where (:orderNo is null
                   or lower(bookOrder.orderNo) like lower(concat('%', :orderNo, '%')))
              and (:userId is null or bookOrder.user.id = :userId)
              and (:status is null or bookOrder.status = :status)
            order by bookOrder.createTime desc, bookOrder.id desc
            """)
    Page<BookOrder> searchForAdmin(
            @Param("orderNo") String orderNo,
            @Param("userId") Long userId,
            @Param("status") OrderStatus status,
            Pageable pageable
    );

    @Query(value = """
            select count(*) as completedOrderCount,
                   coalesce(sum(payable_amount), 0) as salesAmount
            from book_order
            where status = 'COMPLETED'
            """, nativeQuery = true)
    CompletedSalesSummaryProjection summarizeCompletedSales();

    @Query(value = """
            select date_format(o.completed_time, '%Y-%m') as saleMonth,
                   count(distinct o.id) as completedOrderCount,
                   coalesce(sum(oi.quantity), 0) as soldQuantity,
                   coalesce(sum(oi.subtotal), 0) as salesAmount
            from book_order o
            join order_item oi on oi.order_id = o.id
            where o.status = 'COMPLETED'
              and o.completed_time >= :startTime
            group by date_format(o.completed_time, '%Y-%m')
            order by saleMonth
            """, nativeQuery = true)
    java.util.List<MonthlySalesProjection> summarizeMonthlySales(
            @Param("startTime") LocalDateTime startTime
    );

    @Query(value = """
            select date_format(o.completed_time, '%Y-%m-%d') as saleDate,
                   coalesce(sum(oi.quantity), 0) as soldQuantity,
                   coalesce(sum(oi.subtotal), 0) as salesAmount
            from book_order o
            join order_item oi on oi.order_id = o.id
            where o.status = 'COMPLETED'
              and o.completed_time >= :startTime
            group by date_format(o.completed_time, '%Y-%m-%d')
            order by saleDate
            """, nativeQuery = true)
    java.util.List<DailySalesProjection> summarizeDailySales(
            @Param("startTime") LocalDateTime startTime
    );

    @Query(value = """
            select oi.book_id as bookId,
                   oi.book_title as bookTitle,
                   sum(oi.quantity) as soldQuantity,
                   sum(oi.subtotal) as salesAmount
            from order_item oi
            join book_order o on o.id = oi.order_id
            where o.status = 'COMPLETED'
            group by oi.book_id, oi.book_title
            order by soldQuantity desc, salesAmount desc, oi.book_id asc
            limit :top
            """, nativeQuery = true)
    java.util.List<TopBookSalesProjection> findTopSellingBooks(
            @Param("top") int top
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("""
            update BookOrder bookOrder
            set bookOrder.status = :targetStatus,
                bookOrder.cancelledTime = :cancelledTime,
                bookOrder.updateTime = :cancelledTime
            where bookOrder.id = :orderId
              and bookOrder.user.id = :userId
              and bookOrder.status = :expectedStatus
            """)
    int cancelPendingOrder(
            @Param("orderId") Long orderId,
            @Param("userId") Long userId,
            @Param("expectedStatus") OrderStatus expectedStatus,
            @Param("targetStatus") OrderStatus targetStatus,
            @Param("cancelledTime") LocalDateTime cancelledTime
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("""
            update BookOrder bookOrder
            set bookOrder.status = :targetStatus,
                bookOrder.paidTime = :paidTime,
                bookOrder.updateTime = :paidTime
            where bookOrder.id = :orderId
              and bookOrder.user.id = :userId
              and bookOrder.status = :expectedStatus
              and (bookOrder.expireTime is null or bookOrder.expireTime > :paidTime)
            """)
    int payPendingOrder(
            @Param("orderId") Long orderId,
            @Param("userId") Long userId,
            @Param("expectedStatus") OrderStatus expectedStatus,
            @Param("targetStatus") OrderStatus targetStatus,
            @Param("paidTime") LocalDateTime paidTime
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("""
            update BookOrder bookOrder
            set bookOrder.status = :targetStatus,
                bookOrder.shippedTime = :shippedTime,
                bookOrder.updateTime = :shippedTime
            where bookOrder.id = :orderId
              and bookOrder.status = :expectedStatus
            """)
    int shipPendingOrder(
            @Param("orderId") Long orderId,
            @Param("expectedStatus") OrderStatus expectedStatus,
            @Param("targetStatus") OrderStatus targetStatus,
            @Param("shippedTime") LocalDateTime shippedTime
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("""
            update BookOrder bookOrder
            set bookOrder.status = :targetStatus,
                bookOrder.completedTime = :completedTime,
                bookOrder.updateTime = :completedTime
            where bookOrder.id = :orderId
              and bookOrder.user.id = :userId
              and bookOrder.status = :expectedStatus
            """)
    int completeShippedOrder(
            @Param("orderId") Long orderId,
            @Param("userId") Long userId,
            @Param("expectedStatus") OrderStatus expectedStatus,
            @Param("targetStatus") OrderStatus targetStatus,
            @Param("completedTime") LocalDateTime completedTime
    );
}
