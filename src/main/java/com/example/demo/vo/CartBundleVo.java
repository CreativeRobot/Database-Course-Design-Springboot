package com.example.demo.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CartBundleVo {
    private Long id;
    private String name;
    private BigDecimal bundlePrice;
    private BigDecimal regularAmount;
    private BigDecimal savings;
    private List<BookBundleItemVo> items;
    private Boolean applied;
}
