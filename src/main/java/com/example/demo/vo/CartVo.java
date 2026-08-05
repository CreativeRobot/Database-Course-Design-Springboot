package com.example.demo.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class CartVo {
    private List<CartItemVo> items;
    private Integer totalQuantity;
    private Integer selectedQuantity;
    private BigDecimal selectedAmount;
}
