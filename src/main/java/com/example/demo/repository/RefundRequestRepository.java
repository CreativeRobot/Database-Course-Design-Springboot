package com.example.demo.repository;

import com.example.demo.entity.RefundRequest;
import com.example.demo.entity.RefundStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;
import java.math.BigDecimal;

/**
 * RefundRequestRepository 数据访问接口，负责实体持久化及相关查询。
 */
public interface RefundRequestRepository extends JpaRepository<RefundRequest, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from RefundRequest r join fetch r.order join fetch r.orderItem join fetch r.user where r.id = :id")
    java.util.Optional<RefundRequest> findByIdForUpdate(@Param("id") Long id);
    java.util.Optional<RefundRequest> findByIdAndUser_Id(Long id, Long userId);
    Page<RefundRequest> findByUser_IdOrderByCreateTimeDesc(Long userId, Pageable pageable);
    @Query("select coalesce(sum(r.quantity), 0) from RefundRequest r where r.orderItem.id = :itemId and r.status = :status")
    Integer sumQuantityByOrderItemIdAndStatus(@Param("itemId") Long itemId, @Param("status") RefundStatus status);

    @Query("select coalesce(sum(r.quantity), 0) from RefundRequest r where r.orderItem.id = :itemId and r.status in (com.example.demo.entity.RefundStatus.PENDING, com.example.demo.entity.RefundStatus.APPROVED)")
    Integer sumApprovedOrPendingQuantity(@Param("itemId") Long itemId);

    @Query("select count(r) > 0 from RefundRequest r where r.orderItem.id = :itemId and r.bundleAware = false and r.status in (com.example.demo.entity.RefundStatus.PENDING, com.example.demo.entity.RefundStatus.APPROVED)")
    boolean existsLegacyActiveForOrderItem(@Param("itemId") Long itemId);
    @Query("select coalesce(sum(r.amount), 0) from RefundRequest r where r.order.id = :orderId and r.status = com.example.demo.entity.RefundStatus.APPROVED")
    BigDecimal sumApprovedAmount(@Param("orderId") Long orderId);
    @Query("select r from RefundRequest r join fetch r.order join fetch r.orderItem join fetch r.user where (:status is null or r.status = :status) and (:type is null or r.type = :type) order by r.createTime desc, r.id desc")
    Page<RefundRequest> searchForAdmin(@Param("status") RefundStatus status, @Param("type") com.example.demo.entity.RefundType type, Pageable pageable);
}

