package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "bundle_refund_request_item", uniqueConstraints = @UniqueConstraint(
        name = "uk_bundle_refund_request_item", columnNames = {"request_id", "order_item_id"}))
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class BundleRefundRequestItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "request_id", nullable = false)
    private BundleRefundRequest request;
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_item_id", nullable = false)
    private OrderItem orderItem;
    @Column(nullable = false) private Long bookId;
    @Column(nullable = false, length = 200) private String bookTitle;
    @Column(nullable = false, length = 20) private String isbn;
    @Column(nullable = false, precision = 10, scale = 2) private BigDecimal salePrice;
    @Column(nullable = false, precision = 10, scale = 2) private BigDecimal allocatedDiscount;
    @Column(nullable = false) private Integer quantity;
    @Column(nullable = false, precision = 10, scale = 2) private BigDecimal amount;
}
