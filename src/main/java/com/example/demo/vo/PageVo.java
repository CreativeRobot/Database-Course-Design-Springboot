package com.example.demo.vo;

import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 通用分页视图对象。
 * page 为 1 起始的页码，便于前端直接展示。
 */
@Data
public class PageVo<T> {
    private List<T> records;
    private long total;
    private int page;
    private int size;
    private int totalPages;

    public static <T> PageVo<T> of(Page<T> pageData) {
        PageVo<T> vo = new PageVo<>();
        vo.setRecords(pageData.getContent());
        vo.setTotal(pageData.getTotalElements());
        vo.setPage(pageData.getNumber() + 1);
        vo.setSize(pageData.getSize());
        vo.setTotalPages(pageData.getTotalPages());
        return vo;
    }
}
