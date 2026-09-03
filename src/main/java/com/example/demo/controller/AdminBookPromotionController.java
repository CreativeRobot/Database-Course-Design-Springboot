package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.dto.*;
import com.example.demo.service.BookPromotionService;
import com.example.demo.vo.BookPromotionVo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/promotions")
public class AdminBookPromotionController {
    @Autowired private BookPromotionService promotionService;

    @GetMapping public Result<List<BookPromotionVo>> list() { return Result.success(promotionService.listAdmin()); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public Result<BookPromotionVo> create(@Valid @RequestBody CreateBookPromotionDTO dto) { return Result.success(promotionService.create(dto)); }
    @PutMapping("/{id}") public Result<BookPromotionVo> update(@PathVariable Long id, @Valid @RequestBody UpdateBookPromotionDTO dto) { return Result.success(promotionService.update(id, dto)); }
    @PutMapping("/{id}/status") public Result<BookPromotionVo> status(@PathVariable Long id, @Valid @RequestBody BookPromotionStatusDTO dto) { return Result.success(promotionService.changeStatus(id, dto.getStatus())); }
}
