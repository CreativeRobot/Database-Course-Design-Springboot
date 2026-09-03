package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.service.BookBundleService;
import com.example.demo.vo.CustomerBookBundleVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 图书详情页展示当前图书可购买组合包。 */
@RestController
@RequestMapping("/api/books")
public class BookBundleController {
    @Autowired
    private BookBundleService bookBundleService;

    @GetMapping("/{bookId}/bundles")
    public Result<List<CustomerBookBundleVo>> listForBook(@PathVariable Long bookId) {
        return Result.success(bookBundleService.listForBook(bookId));
    }
}
