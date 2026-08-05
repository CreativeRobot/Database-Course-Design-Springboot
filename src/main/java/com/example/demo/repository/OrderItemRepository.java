package com.example.demo.repository;

import com.example.demo.entity.OrderItem;
import com.example.demo.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrder_IdOrderByIdAsc(Long orderId);

    List<OrderItem> findByBook_IdOrderByIdDesc(Long bookId);

    Optional<OrderItem> findByIdAndOrder_User_Id(Long orderItemId, Long userId);

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

    @Query(value = """
            select coalesce(sum(oi.quantity), 0)
            from order_item oi
            join book_order o on o.id = oi.order_id
            where o.status = 'COMPLETED'
            """, nativeQuery = true)
    Long sumCompletedSoldQuantity();

    @Query(value = """
            select c.id as categoryId,
                   c.name as categoryName,
                   sum(oi.quantity) as soldQuantity,
                   sum(oi.subtotal) as salesAmount
            from order_item oi
            join book_order o on o.id = oi.order_id
            join book_category bc on bc.book_id = oi.book_id
            join category c on c.id = bc.category_id
            where o.status = 'COMPLETED'
            group by c.id, c.name
            order by soldQuantity desc, salesAmount desc, c.id asc
            """, nativeQuery = true)
    List<CategorySalesProjection> findCategorySales();
}
