package com.example.demo.service;

import com.example.demo.common.exception.BusinessException;
import com.example.demo.dto.CreateReviewDTO;
import com.example.demo.entity.Book;
import com.example.demo.entity.BookOrder;
import com.example.demo.entity.BookReview;
import com.example.demo.entity.OrderItem;
import com.example.demo.entity.OrderStatus;
import com.example.demo.entity.User;
import com.example.demo.repository.BookRepository;
import com.example.demo.repository.BookReviewRepository;
import com.example.demo.repository.OrderItemRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.vo.ReviewVo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTests {

    @Mock
    private BookReviewRepository bookReviewRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RecommendationService recommendationService;

    @InjectMocks
    private ReviewService reviewService;

    private User user;
    private Book book;
    private CreateReviewDTO dto;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("buyer").nickname("读者").status(1).build();
        book = Book.builder().id(10L).title("数据库系统概论").build();
        dto = new CreateReviewDTO();
        dto.setOrderItemId(40L);
        dto.setRating(5);
        dto.setContent("内容清晰");

        lenient().when(userRepository.findByIdAndStatus(1L, 1)).thenReturn(Optional.of(user));
    }

    @Test
    void createReviewForCompletedOrder() {
        OrderItem item = buildOrderItem(OrderStatus.COMPLETED);
        when(orderItemRepository.findByIdAndOrder_User_Id(40L, 1L))
                .thenReturn(Optional.of(item));
        when(bookReviewRepository.existsByOrderItem_Id(40L)).thenReturn(false);
        when(bookReviewRepository.saveAndFlush(any(BookReview.class))).thenAnswer(invocation -> {
            BookReview review = invocation.getArgument(0);
            review.setId(60L);
            return review;
        });

        ReviewVo result = reviewService.createReview(1L, dto);

        assertEquals(5, result.getRating());
        assertEquals("读者", result.getReviewerName());
        assertEquals(40L, result.getOrderItemId());
        verify(recommendationService).invalidateAllAfterCommit();
    }

    @Test
    void createReviewRejectsOrderThatIsNotCompleted() {
        OrderItem item = buildOrderItem(OrderStatus.SHIPPED);
        when(orderItemRepository.findByIdAndOrder_User_Id(40L, 1L))
                .thenReturn(Optional.of(item));

        BusinessException exception = assertThrows(
                BusinessException.class, () -> reviewService.createReview(1L, dto));

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        verify(bookReviewRepository, never()).saveAndFlush(any());
    }

    @Test
    void createReviewRejectsDuplicateOrderItem() {
        OrderItem item = buildOrderItem(OrderStatus.COMPLETED);
        when(orderItemRepository.findByIdAndOrder_User_Id(40L, 1L))
                .thenReturn(Optional.of(item));
        when(bookReviewRepository.existsByOrderItem_Id(40L)).thenReturn(true);

        BusinessException exception = assertThrows(
                BusinessException.class, () -> reviewService.createReview(1L, dto));

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        verify(bookReviewRepository, never()).saveAndFlush(any());
    }

    @Test
    void changingReviewVisibilityInvalidatesTheReviewersRecommendations() {
        OrderItem item = buildOrderItem(OrderStatus.COMPLETED);
        BookReview review = BookReview.builder()
                .id(60L).user(user).book(book).orderItem(item).status(1).rating(5).build();
        when(bookReviewRepository.findById(60L)).thenReturn(Optional.of(review));
        when(bookReviewRepository.save(any(BookReview.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ReviewVo result = reviewService.changeReviewStatus(60L, 0);

        assertEquals(0, result.getStatus());
        verify(recommendationService).invalidateAllAfterCommit();
    }

    private OrderItem buildOrderItem(OrderStatus status) {
        BookOrder order = BookOrder.builder().id(30L).user(user).status(status).build();
        return OrderItem.builder()
                .id(40L)
                .order(order)
                .book(book)
                .bookTitle(book.getTitle())
                .quantity(1)
                .build();
    }
}
