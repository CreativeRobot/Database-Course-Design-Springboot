package com.example.demo.vo;

import com.example.demo.entity.BookBundleStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class BookBundleVo {
    private Long id;
    private String name;
    private String description;
    private BigDecimal bundlePrice;
    private BigDecimal regularAmount;
    private BigDecimal savings;
    private BookBundleStatus status;
    private Boolean homePinned;
    private Integer homePriority;
    private Long version;
    private Boolean priceValid;
    private Boolean purchasable;
    private String unavailableReason;
    private List<BookBundleItemVo> items;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
