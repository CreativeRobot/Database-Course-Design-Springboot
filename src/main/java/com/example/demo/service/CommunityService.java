package com.example.demo.service;

import com.example.demo.common.exception.BusinessException;
import com.example.demo.dto.CreateCommunityCommentDTO;
import com.example.demo.dto.CreateCommunityPostDTO;
import com.example.demo.entity.Book;
import com.example.demo.entity.BookStatus;
import com.example.demo.entity.CommunityComment;
import com.example.demo.entity.CommunityPost;
import com.example.demo.entity.CommunityPostBook;
import com.example.demo.entity.CommunityPostBookId;
import com.example.demo.entity.CommunityPostImage;
import com.example.demo.entity.User;
import com.example.demo.repository.BookRepository;
import com.example.demo.repository.CommunityCommentRepository;
import com.example.demo.repository.CommunityPostRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.vo.CommunityCommentVo;
import com.example.demo.vo.CommunityPostVo;
import com.example.demo.vo.PageVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
public class CommunityService {
    private static final int ACTIVE = 1;
    private static final int MAX_PAGE_SIZE = 50;
    private static final int MAX_IMAGES = 9;

    @Autowired
    private CommunityPostRepository postRepository;
    @Autowired
    private CommunityCommentRepository commentRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BookRepository bookRepository;

    @Transactional(readOnly = true)
    public PageVo<CommunityPostVo> listPosts(String keyword, Long bookId, int page, int size) {
        Pageable pageable = pageRequest(page, size);
        String normalizedKeyword = StringUtils.hasText(keyword) ? keyword.trim() : null;
        return PageVo.of(postRepository.search(normalizedKeyword, bookId, pageable).map(this::toPostVo));
    }

    @Transactional(readOnly = true)
    public CommunityPostVo getPost(Long postId) {
        return toPostVo(getVisiblePost(postId));
    }

    @Transactional(readOnly = true)
    public PageVo<CommunityPostVo> listAdminPosts(
            String keyword, Long userId, Integer status, int page, int size) {
        if (userId != null && userId <= 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "用户标识不合法");
        }
        if (status != null && status != 0 && status != ACTIVE) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "帖子状态只能为0或1");
        }
        String normalizedKeyword = StringUtils.hasText(keyword) ? keyword.trim() : null;
        return PageVo.of(postRepository.searchForAdmin(
                normalizedKeyword, userId, status, pageRequest(page, size)).map(this::toPostVo));
    }

    @Transactional
    public CommunityPostVo changePostStatus(Long postId, Integer status) {
        if (status == null || (status != 0 && status != ACTIVE)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "帖子状态只能为0或1");
        }
        if (postId == null || postId <= 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "帖子标识不合法");
        }
        CommunityPost post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "帖子不存在"));
        post.setStatus(status);
        return toPostVo(postRepository.save(post));
    }


    @Transactional
    public CommunityPostVo createPost(Long userId, CreateCommunityPostDTO dto) {
        validatePost(userId, dto);
        User user = getActiveUser(userId);
        List<String> imageUrls = dto.getImageUrls() == null ? List.of() : dto.getImageUrls();
        List<Long> bookIds = normalizeIds(dto.getBookIds());
        List<Book> books = loadBooks(bookIds);

        CommunityPost post = CommunityPost.builder()
                .user(user)
                .title(dto.getTitle().trim())
                .content(dto.getContent().trim())
                .status(ACTIVE)
                .build();
        for (int index = 0; index < imageUrls.size(); index++) {
            post.getImages().add(CommunityPostImage.builder()
                    .post(post)
                    .imageUrl(imageUrls.get(index).trim())
                    .sortOrder(index)
                    .build());
        }
        for (Book book : books) {
            post.getBookLinks().add(CommunityPostBook.builder()
                    .id(new CommunityPostBookId(null, book.getId()))
                    .post(post)
                    .book(book)
                    .build());
        }
        return toPostVo(postRepository.save(post));
    }

    @Transactional(readOnly = true)
    public PageVo<CommunityCommentVo> listComments(Long postId, int page, int size) {
        getVisiblePost(postId);
        return PageVo.of(commentRepository.findByPost_IdAndStatus(
                postId, ACTIVE, pageRequest(page, size)).map(this::toCommentVo));
    }

    @Transactional
    public CommunityCommentVo createComment(Long userId, Long postId, CreateCommunityCommentDTO dto) {
        if (dto == null || !StringUtils.hasText(dto.getContent()) || dto.getContent().trim().length() > 1000) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "评论内容不能为空且不能超过1000个字符");
        }
        CommunityPost post = getVisiblePost(postId);
        User user = getActiveUser(userId);
        CommunityComment parent = null;
        if (dto.getParentId() != null) {
            parent = commentRepository.findVisibleByIdAndPostId(dto.getParentId(), postId)
                    .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, "回复的评论不存在或不属于当前帖子"));
        }
        CommunityComment comment = CommunityComment.builder()
                .post(post)
                .user(user)
                .parent(parent)
                .content(dto.getContent().trim())
                .status(ACTIVE)
                .build();
        return toCommentVo(commentRepository.save(comment));
    }

    private void validatePost(Long userId, CreateCommunityPostDTO dto) {
        if (dto == null || !StringUtils.hasText(dto.getTitle()) || dto.getTitle().trim().length() > 120) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "帖子标题不能为空且不能超过120个字符");
        }
        if (!StringUtils.hasText(dto.getContent()) || dto.getContent().trim().length() > 5000) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "帖子正文不能为空且不能超过5000个字符");
        }
        List<String> imageUrls = dto.getImageUrls() == null ? List.of() : dto.getImageUrls();
        if (imageUrls.size() > MAX_IMAGES || imageUrls.stream().anyMatch(url -> !isOwnedPostImage(userId, url))) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "帖子最多添加9张当前用户上传的有效图片");
        }
    }

    private boolean isOwnedPostImage(Long userId, String url) {
        if (userId == null || userId <= 0 || !StringUtils.hasText(url)) return false;
        String prefix = "/uploads/posts/" + userId + "/";
        String filename = url.trim().startsWith(prefix) ? url.trim().substring(prefix.length()) : "";
        return !filename.isBlank()
                && !filename.contains("..")
                && !filename.contains("/")
                && !filename.contains("\\");
    }
    private List<Long> normalizeIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) return List.of();
        if (ids.stream().anyMatch(Objects::isNull)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "关联图书不能为空");
        }
        return ids.stream().distinct().toList();
    }

    private List<Book> loadBooks(List<Long> bookIds) {
        if (bookIds.isEmpty()) return List.of();
        List<Book> books = bookRepository.findAllById(bookIds);
        Set<Long> found = new HashSet<>();
        for (Book book : books) found.add(book.getId());
        if (found.size() != bookIds.size() || !found.containsAll(bookIds)
                || books.stream().anyMatch(book -> book.getStatus() != BookStatus.ON_SALE)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "关联图书不存在或已下架");
        }
        Map<Long, Book> byId = new HashMap<>();
        for (Book book : books) byId.put(book.getId(), book);
        return bookIds.stream().map(byId::get).toList();
    }

    private CommunityPost getVisiblePost(Long postId) {
        if (postId == null || postId <= 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "帖子标识不合法");
        }
        return postRepository.findVisibleById(postId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "帖子不存在或已隐藏"));
    }

    private User getActiveUser(Long userId) {
        if (userId == null || userId <= 0) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "未登录或用户信息无效");
        }
        return userRepository.findByIdAndStatus(userId, ACTIVE)
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "未登录或用户已被禁用"));
    }

    private Pageable pageRequest(int page, int size) {
        if (page < 1 || size < 1 || size > MAX_PAGE_SIZE) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "分页参数不合法");
        }
        return PageRequest.of(page - 1, size,
                Sort.by(Sort.Direction.DESC, "createTime")
                        .and(Sort.by(Sort.Direction.DESC, "id")));
    }

    private CommunityPostVo toPostVo(CommunityPost post) {
        CommunityPostVo vo = new CommunityPostVo();
        vo.setId(post.getId());
        User user = post.getUser();
        if (user != null) {
            vo.setUserId(user.getId());
            vo.setAuthorName(StringUtils.hasText(user.getNickname()) ? user.getNickname() : user.getUsername());
            vo.setAuthorAvatar(user.getAvatarUrl());
        }
        vo.setTitle(post.getTitle());
        vo.setContent(post.getContent());
        vo.setStatus(post.getStatus());
        vo.setCreateTime(post.getCreateTime());
        vo.setUpdateTime(post.getUpdateTime());
        if (post.getImages() != null) {
            vo.setImageUrls(post.getImages().stream().map(CommunityPostImage::getImageUrl).toList());
        }
        if (post.getBookLinks() != null) {
            vo.setBookIds(post.getBookLinks().stream().map(link -> link.getBook().getId()).toList());
            vo.setBookTitles(post.getBookLinks().stream().map(link -> link.getBook().getTitle()).toList());
        }
        if (post.getId() != null) vo.setCommentCount(commentRepository.countByPost_IdAndStatus(post.getId(), ACTIVE));
        return vo;
    }

    private CommunityCommentVo toCommentVo(CommunityComment comment) {
        CommunityCommentVo vo = new CommunityCommentVo();
        vo.setId(comment.getId());
        vo.setPostId(comment.getPost() == null ? null : comment.getPost().getId());
        vo.setUserId(comment.getUser() == null ? null : comment.getUser().getId());
        if (comment.getUser() != null) {
            vo.setAuthorName(StringUtils.hasText(comment.getUser().getNickname())
                    ? comment.getUser().getNickname() : comment.getUser().getUsername());
            vo.setAuthorAvatar(comment.getUser().getAvatarUrl());
        }
        vo.setParentId(comment.getParent() == null ? null : comment.getParent().getId());
        vo.setContent(comment.getContent());
        vo.setStatus(comment.getStatus());
        vo.setCreateTime(comment.getCreateTime());
        vo.setUpdateTime(comment.getUpdateTime());
        return vo;
    }
}
