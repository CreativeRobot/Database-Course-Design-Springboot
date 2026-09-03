package com.example.demo.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * OrderItem 数据库实体，映射业务领域中的持久化数据。
 */
@Entity
@Table(
        name = "order_item",
        indexes = {
                @Index(name = "idx_order_item_order", columnList = "order_id"),
                @Index(name = "idx_order_item_book", columnList = "book_id")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private BookOrder order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    //保存购买时信息
    @Column(nullable = false, length = 200)
    private String bookTitle;

    @Column(nullable = false, length = 20)
    private String isbn;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;//购买时价格

    @Column(nullable = false)
    private Integer quantity;//数量

    @Builder.Default
    @Column(nullable = false)
    private Integer refundedQuantity = 0;//累计退货/退款数量

    @Builder.Default
    @Column(nullable = false)
    private Boolean preSale = false;//下单时是否为预售

    private LocalDateTime preSaleReleaseTime;//下单时预计发售时间快照

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;//普通售价小计

    @Builder.Default
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;//组合包优惠分摊

    @Builder.Default
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal paidSubtotal = BigDecimal.ZERO;//实际支付小计
}



