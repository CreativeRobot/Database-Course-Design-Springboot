package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.dto.BookCreateDTO;
import com.example.demo.dto.BookStatusDTO;
import com.example.demo.dto.BookUpdateDTO;
import com.example.demo.dto.StockAdjustDTO;
import com.example.demo.entity.BookStatus;
import com.example.demo.service.BookService;
import com.example.demo.vo.BookDetailVo;
import com.example.demo.vo.BookVo;
import com.example.demo.vo.PageVo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 图书管理端接口。
 * 路径位于 /api/admin/** 下，经过 JwtInterceptor + AdminInterceptor，仅 ADMIN 可访问。
 */
@RestController
@RequestMapping("/api/admin/books")
public class AdminBookController {

    @Autowired
    private BookService bookService;

    /** 分页查询全部图书，可按状态、作者、出版社或分类过滤（不传参数则查全部） */
    @GetMapping
    public Result<PageVo<BookVo>> listBooks(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) BookStatus status,
            @RequestParam(required = false) Long authorId,
            @RequestParam(required = false) Long publisherId,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(bookService.listAllBooks(
                keyword, status, authorId, publisherId, categoryId, page, size));
    }

    /** 查询图书详情（含已下架图书） */
    @GetMapping("/{bookId}")
    public Result<BookDetailVo> getBookDetail(@PathVariable Long bookId) {
        return Result.success(bookService.getBookDetail(bookId));
    }

    /** 库存预警：查询库存不高于阈值的在售图书 */
    @GetMapping("/low-stock")
    public Result<List<BookVo>> listLowStockBooks(
            @RequestParam(defaultValue = "10") int threshold) {
        return Result.success(bookService.listLowStockBooks(threshold));
    }

    /** 新增图书（含作者、分类关联和初始库存流水） */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Result<BookDetailVo> createBook(@Valid @RequestBody BookCreateDTO createDTO) {
        return Result.success(bookService.createBook(createDTO));
    }

    /** 更新图书信息，null 字段不修改；authorIds/categoryIds 非 null 时整体替换 */
    @PutMapping("/{bookId}")
    public Result<BookDetailVo> updateBook(
            @PathVariable Long bookId,
            @Valid @RequestBody BookUpdateDTO updateDTO) {
        return Result.success(bookService.updateBook(bookId, updateDTO));
    }

    /** 上架/下架图书 */
    @PutMapping("/{bookId}/status")
    public Result<BookDetailVo> changeStatus(
            @PathVariable Long bookId,
            @Valid @RequestBody BookStatusDTO statusDTO) {
        return Result.success(bookService.changeStatus(bookId, statusDTO.getStatus()));
    }

    /** 下架图书（语义化的删除：保留历史订单数据，不做物理删除） */
    @DeleteMapping("/{bookId}")
    public Result<BookDetailVo> takeDownBook(@PathVariable Long bookId) {
        return Result.success(bookService.changeStatus(bookId, BookStatus.OFF_SALE));
    }

    /** 手动调整库存（正数入库/负数出库），写入库存流水 */
    @PutMapping("/{bookId}/stock")
    public Result<BookDetailVo> adjustStock(
            @PathVariable Long bookId,
            @Valid @RequestBody StockAdjustDTO stockAdjustDTO) {
        return Result.success(bookService.adjustStock(bookId, stockAdjustDTO));
    }
}
