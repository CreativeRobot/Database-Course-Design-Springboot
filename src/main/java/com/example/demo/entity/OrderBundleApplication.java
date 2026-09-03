package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_bundle_application", uniqueConstraints = @UniqueConstraint(
        name = "uk_order_bundle_application_order_bundle", columnNames = {"order_id", "bundle_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderBundleApplication {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private BookOrder order;

    @Column(nullable = false)
    private Long bundleId;

    @Column(nullable = false, length = 100)
    private String bundleName;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal bundlePrice;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal regularAmount;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal discountAmount;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createTime;
}
