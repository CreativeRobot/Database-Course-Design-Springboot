package com.example.demo.repository;

import com.example.demo.entity.BundleRefundRequestItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BundleRefundRequestItemRepository extends JpaRepository<BundleRefundRequestItem, Long> {
    List<BundleRefundRequestItem> findByRequest_IdOrderByIdAsc(Long requestId);
    List<BundleRefundRequestItem> findByRequest_IdOrderByOrderItem_IdAsc(Long requestId);
}
