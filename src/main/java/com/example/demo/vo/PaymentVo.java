package com.example.demo.vo;

import com.example.demo.entity.PaymentMethod;
import com.example.demo.entity.PaymentStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * PaymentVo 响应视图对象，用于封装返回给客户端的数据。
 */
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
