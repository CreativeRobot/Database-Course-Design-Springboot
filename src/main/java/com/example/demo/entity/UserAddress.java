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
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * UserAddress 数据库实体，映射业务领域中的持久化数据。
 */
@Entity
@Table(name = "user_address")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 50)
    private String receiverName;//收货人

    @Column(nullable = false, length = 20)
    private String receiverPhone;//收货手机号

    @Column(nullable = false, length = 50)
    private String province;//省份

    @Column(nullable = false, length = 50)
    private String city;//城市

    @Column(length = 50)
    private String district;//区（县）允许为空

    @Column(nullable = false, length = 255)
    private String detailAddress;//详细地址

    @Column(length = 10)
    private String postalCode;//邮政编码（可选）

    @Builder.Default
    @Column(nullable = false)
    private Boolean defaultAddress = false;//当前地址是否默认地址

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createTime;

    @UpdateTimestamp
    private LocalDateTime updateTime;
}
