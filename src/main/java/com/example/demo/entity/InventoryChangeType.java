package com.example.demo.entity;

public enum InventoryChangeType {
    PURCHASE_IN,//供应商供货导致库存增加
    ORDER_OUT,//用户下单导致库存减少
    ORDER_CANCEL_RETURN,//订单取消退回导致库存增加
    REFUND_RETURN,//退货退款导致库存增加
    MANUAL_ADJUSTMENT//管理员修改
}
