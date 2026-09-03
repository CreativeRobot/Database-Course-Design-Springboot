package com.example.demo.vo;

import com.example.demo.entity.BookStatus;
import lombok.Data;

/**
 * LowStockBookVo 响应视图对象，用于封装返回给客户端的数据。
 */
@Data
public class LowStockBookVo {
    private Long bookId;
    private String isbn;
    private String title;
    private Integer stock;
    private BookStatus status;
}
