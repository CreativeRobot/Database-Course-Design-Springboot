package com.example.demo.service;

import com.example.demo.common.exception.BusinessException;
import com.example.demo.dto.CreateCommunityCommentDTO;
import com.example.demo.dto.CreateCommunityPostDTO;
import com.example.demo.entity.Book;
import com.example.demo.entity.BookStatus;
import com.example.demo.entity.CommunityPostImage;
import com.example.demo.entity.CommunityComment;
import com.example.demo.entity.CommunityPost;
import com.example.demo.entity.User;
import com.example.demo.repository.BookRepository;
import com.example.demo.repository.CommunityCommentRepository;
import com.example.demo.repository.CommunityPostRepository;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommunityServiceTests {

    @Mock
    private CommunityPostRepository postRepository;
    @Mock
    private CommunityCommentRepository commentRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private CommunityService communityService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("reader").nickname("读者").status(1).build();
        lenient().when(userRepository.findByIdAndStatus(1L, 1)).thenReturn(Optional.of(user));
    }

    @Test
    void rejectsBlankPostTitle() {
        CreateCommunityPostDTO dto = postDto(" ", "正文", List.of(), List.of());

        BusinessException exception = assertThrows(
                BusinessException.class, () -> communityService.createPost(1L, dto));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verify(postRepository, never()).save(any());
    }

    @Test
    void rejectsMoreThanNineImages() {
        CreateCommunityPostDTO dto = postDto(
                "读书分享", "正文", List.of("1", "2", "3", "4", "5", "6", "7", "8", "9", "10"), List.of());

        BusinessException exception = assertThrows(
                BusinessException.class, () -> communityService.createPost(1L, dto));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verify(postRepository, never()).save(any());
    }

    @Test
    void rejectsUnknownRelatedBook() {
        CreateCommunityPostDTO dto = postDto("读书分享", "正文", List.of(), List.of(10L, 11L));
        when(bookRepository.findAllById(List.of(10L, 11L))).thenReturn(List.of(book(10L)));

        BusinessException exception = assertThrows(
                BusinessException.class, () -> communityService.createPost(1L, dto));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verify(postRepository, never()).save(any());
    }

    @Test
    void rejectsReplyToCommentFromAnotherPost() {
        CreateCommunityCommentDTO dto = new CreateCommunityCommentDTO();
        dto.setContent("回复");
        dto.setParentId(99L);
        when(postRepository.findVisibleById(5L)).thenReturn(Optional.of(post(5L)));
        when(commentRepository.findVisibleByIdAndPostId(99L, 5L)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class, () -> communityService.createComment(1L, 5L, dto));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verify(commentRepository, never()).save(any());
    }

    @Test
    void createsPostWhenOptionalCollectionsAreNull() {
        CreateCommunityPostDTO dto = postDto("读书分享", "正文", null, null);
        when(postRepository.save(any(CommunityPost.class))).thenAnswer(invocation -> {
            CommunityPost post = invocation.getArgument(0);
            post.setId(6L);
            return post;
        });

        var result = communityService.createPost(1L, dto);

        assertEquals(6L, result.getId());
        assertEquals(List.of(), result.getImageUrls());
        assertEquals(List.of(), result.getBookIds());
    }

    @Test
    void createsPostWithRelatedBooksAndImages() {
        CreateCommunityPostDTO dto = postDto("读书分享", "正文", List.of("/uploads/posts/1/a.jpg"), List.of(10L));
        when(bookRepository.findAllById(List.of(10L))).thenReturn(List.of(book(10L)));
        when(postRepository.save(any(CommunityPost.class))).thenAnswer(invocation -> {
            CommunityPost post = invocation.getArgument(0);
            post.setId(5L);
            return post;
        });

        var result = communityService.createPost(1L, dto);

        assertEquals(5L, result.getId());
        assertEquals("读书分享", result.getTitle());
        assertEquals(1, result.getImageUrls().size());
        assertEquals(List.of(10L), result.getBookIds());
    }

    @Test
    void rejectsImageOwnedByAnotherUser() {
        CreateCommunityPostDTO dto = postDto(
                "读书分享", "正文", List.of("/uploads/posts/2/a.jpg"), List.of());

        BusinessException exception = assertThrows(
                BusinessException.class, () -> communityService.createPost(1L, dto));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verify(postRepository, never()).save(any());
    }

    @Test
    void rejectsRelatedBookThatIsNotOnSale() {
        CreateCommunityPostDTO dto = postDto("读书分享", "正文", List.of(), List.of(10L));
        when(bookRepository.findAllById(List.of(10L))).thenReturn(List.of(
                Book.builder().id(10L).title("下架图书").status(BookStatus.OFF_SALE).build()));

        BusinessException exception = assertThrows(
                BusinessException.class, () -> communityService.createPost(1L, dto));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verify(postRepository, never()).save(any());
    }

    @Test
    void adminListIncludesHiddenPosts() {
        CommunityPost hiddenPost = CommunityPost.builder()
                .id(9L)
                .user(user)
                .title("被屏蔽的帖子")
                .content("仍应出现在管理列表")
                .status(0)
                .build();
        when(postRepository.searchForAdmin(eq("屏蔽"), eq(1L), eq(0), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(hiddenPost)));

        var result = communityService.listAdminPosts(" 屏蔽 ", 1L, 0, 1, 20);

        assertEquals(1, result.getRecords().size());
        assertEquals(0, result.getRecords().get(0).getStatus());
        assertEquals("被屏蔽的帖子", result.getRecords().get(0).getTitle());
    }

    @Test
    void hidesPostAndPersistsStatus() {
        CommunityPost target = post(5L);
        when(postRepository.findById(5L)).thenReturn(Optional.of(target));
        when(postRepository.save(target)).thenReturn(target);

        var result = communityService.changePostStatus(5L, 0);

        assertEquals(0, target.getStatus());
        assertEquals(0, result.getStatus());
        verify(postRepository).save(target);
    }

    @Test
    void restoresPostAndPersistsStatus() {
        CommunityPost target = post(5L);
        target.setStatus(0);
        when(postRepository.findById(5L)).thenReturn(Optional.of(target));
        when(postRepository.save(target)).thenReturn(target);

        var result = communityService.changePostStatus(5L, 1);

        assertEquals(1, target.getStatus());
        assertEquals(1, result.getStatus());
        verify(postRepository).save(target);
    }

    @Test
    void rejectsUnsupportedPostStatus() {
        BusinessException exception = assertThrows(
                BusinessException.class, () -> communityService.changePostStatus(5L, 2));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verify(postRepository, never()).findById(any());
    }

    @Test
    void rejectsChangingStatusForUnknownPost() {
        when(postRepository.findById(404L)).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class, () -> communityService.changePostStatus(404L, 0));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        verify(postRepository, never()).save(any());
    }

    @Test
    void communityEntityToStringDoesNotRecurseThroughBidirectionalRelations() {
        CommunityPost post = post(5L);
        CommunityPostImage image = CommunityPostImage.builder()
                .id(8L)
                .post(post)
                .imageUrl("/uploads/posts/1/a.jpg")
                .build();
        post.getImages().add(image);

        assertDoesNotThrow(post::toString);
    }
    private CreateCommunityPostDTO postDto(
            String title, String content, List<String> images, List<Long> bookIds) {
        CreateCommunityPostDTO dto = new CreateCommunityPostDTO();
        dto.setTitle(title);
        dto.setContent(content);
        dto.setImageUrls(images);
        dto.setBookIds(bookIds);
        return dto;
    }

    private Book book(Long id) {
        return Book.builder().id(id).title("图书" + id).coverUrl("/cover.jpg").status(BookStatus.ON_SALE).build();
    }

    private CommunityPost post(Long id) {
        return CommunityPost.builder().id(id).user(user).title("帖子").content("正文").status(1).build();
    }
}
