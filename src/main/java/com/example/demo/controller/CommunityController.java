package com.example.demo.controller;

import com.example.demo.common.Result;
import com.example.demo.dto.CreateCommunityCommentDTO;
import com.example.demo.dto.CreateCommunityPostDTO;
import com.example.demo.service.CommunityService;
import com.example.demo.vo.CommunityCommentVo;
import com.example.demo.vo.CommunityPostVo;
import com.example.demo.vo.PageVo;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CommunityController {
    @Autowired
    private CommunityService communityService;

    @GetMapping("/api/community/posts")
    public Result<PageVo<CommunityPostVo>> listPosts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long bookId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(communityService.listPosts(keyword, bookId, page, size));
    }

    @GetMapping("/api/community/posts/{postId}")
    public Result<CommunityPostVo> getPost(@PathVariable Long postId) {
        return Result.success(communityService.getPost(postId));
    }

    @PostMapping("/api/community/posts")
    @ResponseStatus(HttpStatus.CREATED)
    public Result<CommunityPostVo> createPost(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody CreateCommunityPostDTO dto) {
        return Result.success(communityService.createPost(userId, dto));
    }

    @GetMapping("/api/community/posts/{postId}/comments")
    public Result<PageVo<CommunityCommentVo>> listComments(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        return Result.success(communityService.listComments(postId, page, size));
    }

    @PostMapping("/api/community/posts/{postId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public Result<CommunityCommentVo> createComment(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long postId,
            @Valid @RequestBody CreateCommunityCommentDTO dto) {
        return Result.success(communityService.createComment(userId, postId, dto));
    }
}
