package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "refund_request", indexes = {
        @Index(name = "idx_refund_order", columnList = "order_id"),
        @Index(name = "idx_refund_status", columnList = "status"),
        @Index(name = "idx_refund_create_time", columnList = "create_time")
})
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class RefundRequest {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 40)
    private String refundNo;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private BookOrder order;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private RefundType type;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    @Builder.Default private RefundStatus status = RefundStatus.PENDING;
    @Column(nullable = false) private Integer quantity;
    @Column(nullable = false, precision = 10, scale = 2) private BigDecimal amount;
    @Column(nullable = false, length = 500) private String reason;
    @Column(length = 500) private String reviewRemark;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "reviewer_id")
    private User reviewer;
    private LocalDateTime reviewedTime;
    @CreationTimestamp @Column(updatable = false) private LocalDateTime createTime;
    @UpdateTimestamp private LocalDateTime updateTime;
}
