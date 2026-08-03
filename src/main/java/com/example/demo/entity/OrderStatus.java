package com.example.demo.entity;

public enum OrderStatus {
    PENDING_PAYMENT,//待支付
    PENDING_SHIPMENT,//待发货
    SHIPPED,//已发货
    COMPLETED,//已完成
    CANCELLED//取消
}
