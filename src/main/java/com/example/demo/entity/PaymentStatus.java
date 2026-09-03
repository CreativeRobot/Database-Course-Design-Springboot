package com.example.demo.entity;

/**
 * PaymentStatus 数据库实体，映射业务领域中的持久化数据。
 */
public enum PaymentStatus {
    PENDING,//待支付
    SUCCESS,//支付成功
    FAILED,//失败
    CLOSED,//类似失败
    REFUNDED//退款
}
