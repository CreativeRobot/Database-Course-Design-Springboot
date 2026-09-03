package com.example.demo.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CustomerBookBundleVo {
    private Long id;
    private String name;
    private String description;
    private BigDecimal bundlePrice;
    private BigDecimal regularAmount;
    private BigDecimal savings;
    private List<BookBundleItemVo> items;
}
