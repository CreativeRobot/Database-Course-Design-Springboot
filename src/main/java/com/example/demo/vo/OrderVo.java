package com.example.demo.vo;

import com.example.demo.entity.OrderStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * OrderVo 响应视图对象，用于封装返回给客户端的数据。
 */
@Data
public class OrderVo {
    private Long id;
    private String orderNo;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private BigDecimal discountAmount;
    private BigDecimal shippingFee;
    private BigDecimal payableAmount;
    private LocalDateTime expireTime;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private LocalDateTime paidTime;
    private LocalDateTime shippedTime;
    private LocalDateTime completedTime;
    private LocalDateTime cancelledTime;
    private List<OrderItemVo> items;
    private List<OrderBundleApplicationVo> bundles;
}
