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
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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
