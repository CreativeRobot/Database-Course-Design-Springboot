package com.example.demo.entity;

public enum PaymentStatus {
    PENDING,//待支付
    SUCCESS,//支付成功
    FAILED,//失败
    CLOSED,//类似失败
    REFUNDED//退款
}
