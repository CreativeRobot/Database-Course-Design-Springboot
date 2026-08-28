package com.example.demo.vo;
import com.example.demo.entity.RefundStatus;
import com.example.demo.entity.RefundType;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
public class RefundRequestVo {
    private Long id;
    private String refundNo;
    private Long orderId;
    private String orderNo;
    private Long orderItemId;
    private Long userId;
    private String username;
    private Long bookId;
    private String bookTitle;
    private RefundType type;
    private RefundStatus status;
    private Integer quantity;
    private Integer itemQuantity;
    private Integer refundedQuantity;
    private BigDecimal amount;
    private String reason;
    private String reviewRemark;
    private Long reviewerId;
    private LocalDateTime reviewedTime;
    private LocalDateTime createTime;
}
