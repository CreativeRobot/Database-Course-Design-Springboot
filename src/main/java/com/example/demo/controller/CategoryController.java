package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.service.CategoryService;
import com.example.demo.vo.CategoryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * CategoryController REST 控制器，负责接收请求、调用业务服务并返回统一响应。
 */
@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    // ==================== 接口定义 ====================

    /**
     * 查询并返回当前模块所需的数据。
     */
    @GetMapping("/tree")
    public Result<List<CategoryVo>> listCategoryTree() {
        return Result.success(categoryService.listCategoryTree(1));
    }

    /**
     * 查询并返回当前模块所需的数据。
     */
    @GetMapping
    public Result<List<CategoryVo>> listCategories() {
        return Result.success(categoryService.listCategories(1));
    }
    /** 查询首页展示的热门一级分类。 */
    @GetMapping("/featured")
    public Result<List<CategoryVo>> listFeaturedCategories(
            @RequestParam(defaultValue = "8") int limit) {
        return Result.success(categoryService.listFeaturedCategories(limit));
    }
}

