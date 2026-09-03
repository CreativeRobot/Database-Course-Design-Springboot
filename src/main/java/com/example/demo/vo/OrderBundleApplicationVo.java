package com.example.demo.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class OrderBundleApplicationVo {
    private Long id;
    private Long bundleId;
    private String bundleName;
    private BigDecimal bundlePrice;
    private BigDecimal regularAmount;
    private BigDecimal discountAmount;
    private List<OrderBundleApplicationItemVo> items;
}
