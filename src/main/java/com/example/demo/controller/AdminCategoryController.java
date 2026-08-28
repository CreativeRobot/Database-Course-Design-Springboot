package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.dto.CategoryStatusDTO;
import com.example.demo.dto.SaveCategoryDTO;
import com.example.demo.service.CategoryService;
import com.example.demo.vo.CategoryVo;
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

/** 分类管理端接口，仅管理员可访问。 */
@RestController
@RequestMapping("/api/admin/categories")
public class AdminCategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/tree")
    public Result<List<CategoryVo>> listCategoryTree(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        return Result.success(categoryService.listCategoryTree(keyword, status));
    }

    @GetMapping
    public Result<List<CategoryVo>> listCategories(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {
        return Result.success(categoryService.listCategories(keyword, status));
    }

    @GetMapping("/{categoryId}")
    public Result<CategoryVo> getCategory(@PathVariable Long categoryId) {
        return Result.success(categoryService.getCategory(categoryId));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Result<CategoryVo> createCategory(
            @Valid @RequestBody SaveCategoryDTO dto) {
        return Result.success(categoryService.createCategory(dto));
    }

    @PutMapping("/{categoryId}")
    public Result<CategoryVo> updateCategory(
            @PathVariable Long categoryId,
            @Valid @RequestBody SaveCategoryDTO dto) {
        return Result.success(categoryService.updateCategory(categoryId, dto));
    }

    @PutMapping("/{categoryId}/status")
    public Result<CategoryVo> changeStatus(
            @PathVariable Long categoryId,
            @Valid @RequestBody CategoryStatusDTO dto) {
        return Result.success(categoryService.changeStatus(categoryId, dto.getStatus()));
    }

    @DeleteMapping("/{categoryId}")
    public Result<Void> deleteCategory(@PathVariable Long categoryId) {
        categoryService.deleteCategory(categoryId);
        return Result.success(null);
    }
}
