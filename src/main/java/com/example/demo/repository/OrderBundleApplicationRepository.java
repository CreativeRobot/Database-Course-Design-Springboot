package com.example.demo.repository;

import com.example.demo.entity.OrderBundleApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.List;

public interface OrderBundleApplicationRepository extends JpaRepository<OrderBundleApplication, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from OrderBundleApplication a join fetch a.order where a.id = :id")
    java.util.Optional<OrderBundleApplication> findByIdForUpdate(@Param("id") Long id);
    List<OrderBundleApplication> findByOrder_IdOrderByBundleIdAsc(Long orderId);
}
