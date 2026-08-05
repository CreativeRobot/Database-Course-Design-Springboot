package com.example.demo.vo;

import com.example.demo.entity.BookStatus;
import lombok.Data;

@Data
public class LowStockBookVo {
    private Long bookId;
    private String isbn;
    private String title;
    private Integer stock;
    private BookStatus status;
}
