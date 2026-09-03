package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.dto.BookBundleStatusDTO;
import com.example.demo.dto.CreateBookBundleDTO;
import com.example.demo.dto.UpdateBookBundleDTO;
import com.example.demo.entity.BookBundleStatus;
import com.example.demo.service.BookBundleService;
import com.example.demo.vo.BookBundleVo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 管理员组合包管理接口。 */
@RestController
@RequestMapping("/api/admin/book-bundles")
public class AdminBookBundleController {
    @Autowired
    private BookBundleService bookBundleService;

    @GetMapping
    public Result<List<BookBundleVo>> list() {
        return Result.success(bookBundleService.listAdmin());
    }

    @GetMapping("/{bundleId}")
    public Result<BookBundleVo> get(@PathVariable Long bundleId) {
        return Result.success(bookBundleService.getAdmin(bundleId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Result<BookBundleVo> create(@Valid @RequestBody CreateBookBundleDTO dto) {
        return Result.success(bookBundleService.create(dto));
    }

    @PutMapping("/{bundleId}")
    public Result<BookBundleVo> update(@PathVariable Long bundleId,
                                       @Valid @RequestBody UpdateBookBundleDTO dto) {
        return Result.success(bookBundleService.update(bundleId, dto));
    }

    @PutMapping("/{bundleId}/status")
    public Result<BookBundleVo> changeStatus(@PathVariable Long bundleId,
                                              @Valid @RequestBody BookBundleStatusDTO dto) {
        return Result.success(bookBundleService.changeStatus(bundleId, dto.getStatus()));
    }
}
