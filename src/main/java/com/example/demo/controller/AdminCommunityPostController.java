package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.dto.CommunityPostStatusDTO;
import com.example.demo.service.CommunityService;
import com.example.demo.vo.CommunityPostVo;
import com.example.demo.vo.PageVo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/community/posts")
public class AdminCommunityPostController {
    @Autowired
    private CommunityService communityService;

    @GetMapping
    public Result<PageVo<CommunityPostVo>> listPosts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(
                communityService.listAdminPosts(keyword, userId, status, page, size));
    }

    @PutMapping("/{postId}/status")
    public Result<CommunityPostVo> changeStatus(
            @PathVariable Long postId,
            @Valid @RequestBody CommunityPostStatusDTO dto) {
        return Result.success(communityService.changePostStatus(postId, dto.getStatus()));
    }
}
