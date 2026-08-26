package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.service.CategoryService;
import com.example.demo.vo.CategoryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/tree")
    public Result<List<CategoryVo>> listCategoryTree() {
        return Result.success(categoryService.listCategoryTree(1));
    }

    @GetMapping
    public Result<List<CategoryVo>> listCategories() {
        return Result.success(categoryService.listCategories(1));
    }
}
