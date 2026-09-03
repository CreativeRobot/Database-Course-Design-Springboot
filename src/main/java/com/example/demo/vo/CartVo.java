package com.example.demo.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * CartVo 响应视图对象，用于封装返回给客户端的数据。
 */
@Data
public class CartVo {
    private List<CartItemVo> items;
    private Integer totalQuantity;
    private Integer selectedQuantity;
    private BigDecimal selectedAmount;
    private BigDecimal regularAmount;
    private BigDecimal bundleDiscountAmount;
    private BigDecimal payableAmount;
    private List<CartBundleVo> eligibleBundles;
    private List<CartBundleVo> appliedBundles;
}
