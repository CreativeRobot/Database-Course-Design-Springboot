package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.dto.SaveAuthorDTO;
import com.example.demo.service.AuthorService;
import com.example.demo.vo.AuthorVo;
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

/** 作者管理端接口，仅管理员可访问。 */
@RestController
@RequestMapping("/api/admin/authors")
public class AdminAuthorController {

    @Autowired
    private AuthorService authorService;

    // ==================== 接口定义 ====================

    /**
     * 查询并返回当前模块所需的数据。
     */
    @GetMapping
    public Result<PageVo<AuthorVo>> listAuthors(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(authorService.listAuthors(keyword, page, size));
    }

    /**
     * 查询并返回当前模块所需的数据。
     */
    @GetMapping("/{authorId}")
    public Result<AuthorVo> getAuthor(@PathVariable Long authorId) {
        return Result.success(authorService.getAuthor(authorId));
    }

    /**
     * 创建并保存当前业务数据。
     */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Result<AuthorVo> createAuthor(@Valid @RequestBody SaveAuthorDTO dto) {
        return Result.success(authorService.createAuthor(dto));
    }

    /**
     * 校验请求参数并更新当前业务状态或数据。
     */
    @PutMapping("/{authorId}")
    public Result<AuthorVo> updateAuthor(
            @PathVariable Long authorId,
            @Valid @RequestBody SaveAuthorDTO dto) {
        return Result.success(authorService.updateAuthor(authorId, dto));
    }

    /**
     * 删除或清理当前业务数据。
     */
    @DeleteMapping("/{authorId}")
    public Result<Void> deleteAuthor(@PathVariable Long authorId) {
        authorService.deleteAuthor(authorId);
        return Result.success(null);
    }
}
