package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "inventory_log",
        indexes = {
                @Index(name = "idx_inventory_book", columnList = "book_id"),
                @Index(name = "idx_inventory_order", columnList = "order_id"),
                @Index(name = "idx_inventory_create_time", columnList = "create_time")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(nullable = false)
    private Integer changeQuantity;//每一次库存变动记录

    @Column(nullable = false)
    private Integer beforeStock;//变化前库存

    @Column(nullable = false)
    private Integer afterStock;//变化后库存

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private InventoryChangeType changeType;//枚举类变更原因

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private BookOrder order;//关联订单

    @Column(length = 255)
    private String remark;//备注

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createTime;
}
