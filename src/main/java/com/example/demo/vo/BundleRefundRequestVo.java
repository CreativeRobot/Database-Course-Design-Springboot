package com.example.demo.vo;

import com.example.demo.entity.RefundStatus;
import com.example.demo.entity.RefundType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class BundleRefundRequestVo {
    private Long id;
    private String refundNo;
    private Long orderId;
    private String orderNo;
    private Long bundleApplicationId;
    private Long bundleId;
    private String bundleName;
    private RefundType type;
    private RefundStatus status;
    private BigDecimal amount;
    private String reason;
    private String reviewRemark;
    private Long reviewerId;
    private LocalDateTime reviewedTime;
    private LocalDateTime createTime;
    private List<BundleRefundRequestItemVo> items;
}
