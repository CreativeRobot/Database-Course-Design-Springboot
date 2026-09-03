package com.example.demo.vo;

import lombok.Data;

import java.util.List;

@Data
public class PromotionHomeVo {
    private List<BookVo> discountedBooks;
    private List<CustomerBookBundleVo> bundles;
}
