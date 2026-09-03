package com.example.demo.entity;

/**
 * OrderStatus 数据库实体，映射业务领域中的持久化数据。
 */
public enum OrderStatus {
    PENDING_PAYMENT,//待支付
    PENDING_SHIPMENT,//待发货
    SHIPPED,//已发货
    COMPLETED,//已完成
    CANCELLED//取消
}
