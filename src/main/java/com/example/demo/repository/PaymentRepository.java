package com.example.demo.repository;

import com.example.demo.entity.Payment;
import com.example.demo.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByPaymentNo(String paymentNo);

    boolean existsByPaymentNo(String paymentNo);

    List<Payment> findByOrder_IdOrderByCreateTimeDesc(Long orderId);

    Optional<Payment> findFirstByOrder_IdAndStatusOrderByCreateTimeDesc(
            Long orderId,
            PaymentStatus status
    );
}