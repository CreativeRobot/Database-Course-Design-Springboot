package com.example.demo.repository;

import com.example.demo.entity.OrderBundleApplicationItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderBundleApplicationItemRepository extends JpaRepository<OrderBundleApplicationItem, Long> {
    List<OrderBundleApplicationItem> findByApplication_IdOrderByIdAsc(Long applicationId);
    List<OrderBundleApplicationItem> findByOrderItem_IdOrderByIdAsc(Long orderItemId);
}
