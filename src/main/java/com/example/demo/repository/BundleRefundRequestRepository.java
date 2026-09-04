package com.example.demo.repository;

import com.example.demo.entity.BundleRefundRequest;
import com.example.demo.entity.RefundStatus;
import com.example.demo.entity.RefundType;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BundleRefundRequestRepository extends JpaRepository<BundleRefundRequest, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from BundleRefundRequest r join fetch r.order join fetch r.bundleApplication join fetch r.user where r.id = :id")
    Optional<BundleRefundRequest> findByIdForUpdate(@Param("id") Long id);

    Optional<BundleRefundRequest> findByIdAndUser_Id(Long id, Long userId);
    Page<BundleRefundRequest> findByUser_IdOrderByCreateTimeDesc(Long userId, Pageable pageable);

    @Query("select r from BundleRefundRequest r where r.bundleApplication.id = :applicationId and r.status in :statuses order by r.id desc")
    List<BundleRefundRequest> findByApplicationIdAndStatusIn(@Param("applicationId") Long applicationId,
                                                               @Param("statuses") Collection<RefundStatus> statuses);

    @Query("select r from BundleRefundRequest r join fetch r.order join fetch r.bundleApplication join fetch r.user " +
            "where (:status is null or r.status = :status) and (:type is null or r.type = :type) " +
            "order by r.createTime desc, r.id desc")
    Page<BundleRefundRequest> searchForAdmin(@Param("status") RefundStatus status,
                                              @Param("type") RefundType type,
                                              Pageable pageable);
}
