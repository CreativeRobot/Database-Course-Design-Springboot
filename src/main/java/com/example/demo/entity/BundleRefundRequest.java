package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "bundle_refund_request", indexes = {
        @Index(name = "idx_bundle_refund_order", columnList = "order_id"),
        @Index(name = "idx_bundle_refund_application", columnList = "bundle_application_id"),
        @Index(name = "idx_bundle_refund_user", columnList = "user_id"),
        @Index(name = "idx_bundle_refund_status", columnList = "status"),
        @Index(name = "idx_bundle_refund_create_time", columnList = "create_time")
})
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class BundleRefundRequest {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 40)
    private String refundNo;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private BookOrder order;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bundle_application_id", nullable = false)
    private OrderBundleApplication bundleApplication;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    private RefundType type;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20)
    @Builder.Default private RefundStatus status = RefundStatus.PENDING;
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    @Column(nullable = false, length = 500)
    private String reason;
    @Column(length = 500)
    private String reviewRemark;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "reviewer_id")
    private User reviewer;
    private LocalDateTime reviewedTime;
    @CreationTimestamp @Column(updatable = false)
    private LocalDateTime createTime;
    @UpdateTimestamp private LocalDateTime updateTime;
    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default private List<BundleRefundRequestItem> items = new ArrayList<>();
}
