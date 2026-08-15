package com.example.demo.vo;

import com.example.demo.entity.InventoryChangeType;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class InventoryLogVo {
    private Long id;
    private Long bookId;
    private String isbn;
    private String bookTitle;
    private Integer changeQuantity;
    private Integer beforeStock;
    private Integer afterStock;
    private InventoryChangeType changeType;
    private Long orderId;
    private String orderNo;
    private String remark;
    private LocalDateTime createTime;
}
