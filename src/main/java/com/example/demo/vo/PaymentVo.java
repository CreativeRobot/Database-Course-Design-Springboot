package com.example.demo.vo;

import com.example.demo.entity.PaymentMethod;
import com.example.demo.entity.PaymentStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentVo {
    private Long id;
    private String paymentNo;
    private Long orderId;
    private String orderNo;
    private PaymentMethod paymentMethod;
    private BigDecimal amount;
    private PaymentStatus status;
    private LocalDateTime paidTime;
    private LocalDateTime createTime;
}
