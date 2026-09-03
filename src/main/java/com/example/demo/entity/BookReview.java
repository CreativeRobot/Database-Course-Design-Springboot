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
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * BookReview 数据库实体，映射业务领域中的持久化数据。
 */
@Entity
@Table(
        name = "book_review",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_review_order_item",
                columnNames = "order_item_id"
        ),//一个订单项仅产生一个评价
        indexes = @Index(name = "idx_review_book", columnList = "book_id")
)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_item_id", nullable = false, unique = true)
    private OrderItem orderItem;//必须对应订单项

    @Column(nullable = false)
    private Integer rating;

    @Column(length = 1000)
    private String content;

    @Builder.Default
    @Column(nullable = false)
    private Integer status = 1;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createTime;

    @UpdateTimestamp
    private LocalDateTime updateTime;
}
