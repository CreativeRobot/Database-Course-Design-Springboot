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
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "book_order",
        indexes = {
                @Index(name = "idx_order_user_create_time", columnList = "user_id, create_time"),//用户相关索引
                @Index(name = "idx_order_status", columnList = "status")//订单状态索引
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 32)
    private String orderNo;//订单编号

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;//关联用户

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private OrderStatus status = OrderStatus.PENDING_PAYMENT;//订单状态

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;//总金额

    @Builder.Default
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;//折扣金额

    @Builder.Default
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal shippingFee = BigDecimal.ZERO;//运费

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal payableAmount;//实付金额

    //收货信息
    @Column(nullable = true)
    private LocalDateTime expireTime;

    @Column(nullable = false, length = 50)
    private String receiverName;

    @Column(nullable = false, length = 20)
    private String receiverPhone;

    @Column(nullable = false, length = 255)
    private String receiverAddress;

    @Column(length = 255)
    private String remark;//备注

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createTime;//订单创建时间

    //一系列相关时间
    @UpdateTimestamp
    private LocalDateTime updateTime;

    private LocalDateTime paidTime;

    private LocalDateTime shippedTime;

    private LocalDateTime completedTime;

    private LocalDateTime cancelledTime;
}
