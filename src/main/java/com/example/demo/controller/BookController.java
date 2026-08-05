package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.service.BookService;
import com.example.demo.vo.BookDetailVo;
import com.example.demo.vo.BookVo;
import com.example.demo.vo.PageVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 图书公开接口（无需登录，仅展示在售图书）。
 * GET /api/books 已在 JwtInterceptor 中放行。
 */
@RestController
@RequestMapping("/api/books")
public class BookController {

    @Autowired
    private BookService bookService;

    /**
     * 分页查询在售图书。
     * 可选过滤：keyword（书名模糊）、categoryId、authorId、publisherId，
     * 同时传入多个时只取优先级最高的一个（keyword > categoryId > authorId > publisherId）。
     */
    @GetMapping
    public Result<PageVo<BookVo>> listBooks(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long authorId,
            @RequestParam(required = false) Long publisherId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(bookService.listOnSaleBooks(
                keyword, categoryId, authorId, publisherId, page, size));
    }

    /** 查询在售图书详情，含作者与分类 */
    @GetMapping("/{bookId}")
    public Result<BookDetailVo> getBookDetail(@PathVariable Long bookId) {
        return Result.success(bookService.getOnSaleBookDetail(bookId));
    }
}
