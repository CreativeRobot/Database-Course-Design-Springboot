package com.example.demo.repository;

import com.example.demo.entity.OrderBundleApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderBundleApplicationRepository extends JpaRepository<OrderBundleApplication, Long> {
    List<OrderBundleApplication> findByOrder_IdOrderByBundleIdAsc(Long orderId);
}
