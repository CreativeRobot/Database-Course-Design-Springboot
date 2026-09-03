package com.example.demo.service;

import com.example.demo.common.exception.BusinessException;
import com.example.demo.entity.Book;
import com.example.demo.entity.BookCategory;
import com.example.demo.entity.BookReview;
import com.example.demo.entity.BookStatus;
import com.example.demo.entity.Category;
import com.example.demo.entity.OrderItem;
import com.example.demo.entity.Publisher;
import com.example.demo.entity.OrderItem;
import com.example.demo.repository.BookCategoryRepository;
import com.example.demo.repository.BookRepository;
import com.example.demo.repository.BookReviewRepository;
import com.example.demo.repository.OrderItemRepository;
import com.example.demo.vo.RecommendationHomeVo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceTests {

    @Mock
    private BookRepository bookRepository;
    @Mock
    private BookCategoryRepository bookCategoryRepository;
    @Mock
    private BookReviewRepository bookReviewRepository;
    @Mock
    private OrderItemRepository orderItemRepository;

    @InjectMocks
    private RecommendationService recommendationService;

    @Test
    void returnsPopularBooksForAUserWithoutCompletedOrders() {
        Book popular = book(2L, 30L);
        Book lessPopular = book(1L, 10L);
        when(orderItemRepository.findCompletedByUserId(any(), any())).thenReturn(List.of());
        when(bookRepository.findByStatusAndStockGreaterThan(BookStatus.ON_SALE, 0))
                .thenReturn(List.of(lessPopular, popular));
        when(bookReviewRepository.findByUser_IdAndStatus(7L, 1)).thenReturn(List.of());

        RecommendationHomeVo result = recommendationService.getHomeRecommendations(7L, 12);

        assertEquals("POPULAR", result.getSource());
        assertEquals(2L, result.getBooks().getFirst().getId());
        assertEquals("热门畅销", result.getBooks().getFirst().getReason());
    }

    @Test
    void returnsPopularBooksForAnonymousVisitorsWithoutUserQueries() {
        Book popular = book(2L, 30L);
        when(bookRepository.findByStatusAndStockGreaterThan(BookStatus.ON_SALE, 0))
                .thenReturn(List.of(popular));
        when(bookCategoryRepository.findByBookIdsWithCategory(List.of(2L)))
                .thenReturn(List.of());
        when(bookReviewRepository.findAverageRatingsByBookIds(List.of(2L), 1))
                .thenReturn(List.of());

        RecommendationHomeVo result = recommendationService.getHomeRecommendations(null, 12);

        assertEquals("POPULAR", result.getSource());
        assertEquals(2L, result.getBooks().getFirst().getId());
        verifyNoInteractions(orderItemRepository);
        verify(bookReviewRepository, times(0)).findByUser_IdAndStatus(any(), any());
    }

    @Test
    void recomputesAfterExplicitInvalidationInsteadOfServingCachedResponse() {
        when(orderItemRepository.findCompletedByUserId(any(), any())).thenReturn(List.<OrderItem>of());
        when(bookRepository.findByStatusAndStockGreaterThan(BookStatus.ON_SALE, 0))
                .thenReturn(List.of(book(1L, 1L)));
        when(bookReviewRepository.findByUser_IdAndStatus(7L, 1)).thenReturn(List.of());

        recommendationService.getHomeRecommendations(7L, 12);
        recommendationService.getHomeRecommendations(7L, 12);
        recommendationService.invalidate(7L);
        recommendationService.getHomeRecommendations(7L, 12);

        verify(orderItemRepository, times(2)).findCompletedByUserId(7L, com.example.demo.entity.OrderStatus.COMPLETED);
    }

    @Test
    void recomputesRecommendationsForEveryUserAfterGlobalInvalidation() {
        stubPopularRecommendation();

        recommendationService.getHomeRecommendations(7L, 12);
        recommendationService.getHomeRecommendations(8L, 12);
        recommendationService.invalidateAll();
        recommendationService.getHomeRecommendations(7L, 12);
        recommendationService.getHomeRecommendations(8L, 12);

        verify(orderItemRepository, times(4)).findCompletedByUserId(
                any(), eq(com.example.demo.entity.OrderStatus.COMPLETED));
    }

    @Test
    void honorsEachRequestedLimitWhenTheSameUserHasCachedRecommendations() {
        Book first = book(1L, 20L);
        Book second = book(2L, 10L);
        when(orderItemRepository.findCompletedByUserId(any(), any())).thenReturn(List.of());
        when(bookRepository.findByStatusAndStockGreaterThan(BookStatus.ON_SALE, 0))
                .thenReturn(List.of(first, second));
        when(bookReviewRepository.findByUser_IdAndStatus(7L, 1)).thenReturn(List.of());

        RecommendationHomeVo oneBook = recommendationService.getHomeRecommendations(7L, 1);
        RecommendationHomeVo twoBooks = recommendationService.getHomeRecommendations(7L, 2);

        assertEquals(1, oneBook.getBooks().size());
        assertEquals(2, twoBooks.getBooks().size());
    }

    @Test
    void rejectsLimitsOutsideThePublicContractBeforeQueryingRecommendations() {
        BusinessException exception = org.junit.jupiter.api.Assertions.assertThrows(
                BusinessException.class, () -> recommendationService.getHomeRecommendations(7L, 0));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verifyNoInteractions(bookRepository, bookCategoryRepository,
                bookReviewRepository, orderItemRepository);
    }

    @Test
    void scoresEachCompletedPurchaseAndReviewOnlyOnce() {
        Book purchased = book(10L, 0L);
        Book categoryMatch = book(20L, 1L);
        Book coPurchasedMatch = book(30L, 0L);
        when(orderItemRepository.findCompletedByUserId(any(), any())).thenReturn(List.of(
                orderItem(purchased, 4), orderItem(purchased, 2)));
        when(bookRepository.findByStatusAndStockGreaterThanAndIdNotIn(
                eq(BookStatus.ON_SALE), eq(0), any())).thenReturn(List.of(categoryMatch, coPurchasedMatch));
        when(bookReviewRepository.findByUser_IdAndStatus(7L, 1)).thenReturn(List.of(
                BookReview.builder().book(purchased).rating(5).build()));
        when(orderItemRepository.findCoPurchasedBookScores(any(), any()))
                .thenReturn(List.<Object[]>of(new Object[]{30L, 10L}));

        RecommendationHomeVo result = recommendationService.getHomeRecommendations(7L, 2);

        assertEquals(30L, result.getBooks().getFirst().getId());
        assertEquals("与已购图书常被一起购买", result.getBooks().getFirst().getReason());
    }

    @Test
    void fallsBackToPopularityWhenNoEligibleCandidateMatchesUserHistory() {
        Book purchased = book(10L, 0L);
        Book unrelated = book(20L, 10L);
        when(orderItemRepository.findCompletedByUserId(any(), any())).thenReturn(List.of(orderItem(purchased, 1)));
        when(bookRepository.findByStatusAndStockGreaterThanAndIdNotIn(
                eq(BookStatus.ON_SALE), eq(0), any())).thenReturn(List.of(unrelated));
        when(bookReviewRepository.findByUser_IdAndStatus(7L, 1)).thenReturn(List.of());
        when(orderItemRepository.findCoPurchasedBookScores(any(), any())).thenReturn(List.of());

        RecommendationHomeVo result = recommendationService.getHomeRecommendations(7L, 1);

        assertEquals("POPULAR", result.getSource());
        assertEquals("热门畅销", result.getBooks().getFirst().getReason());
    }

    @Test
    void loadsCandidateCategoriesAndRatingsInBatches() {
        Book purchased = book(10L, 0L);
        Book categoryMatch = book(20L, 1L);
        Book unrelated = book(30L, 2L);
        when(orderItemRepository.findCompletedByUserId(any(), any())).thenReturn(List.of(orderItem(purchased, 1)));
        when(bookRepository.findByStatusAndStockGreaterThanAndIdNotIn(
                eq(BookStatus.ON_SALE), eq(0), any())).thenReturn(List.of(categoryMatch, unrelated));
        when(bookReviewRepository.findByUser_IdAndStatus(7L, 1)).thenReturn(List.of());
        when(bookCategoryRepository.findByBookIdsWithCategory(List.of(10L, 20L, 30L)))
                .thenReturn(List.of(
                        bookCategory(purchased, 1L),
                        bookCategory(categoryMatch, 1L),
                        bookCategory(unrelated, 2L)));
        when(bookReviewRepository.findAverageRatingsByBookIds(List.of(20L, 30L), 1))
                .thenReturn(List.of(new Object[]{20L, 4.5}, new Object[]{30L, 5.0}));
        when(orderItemRepository.findCoPurchasedBookScores(any(), any())).thenReturn(List.of());

        RecommendationHomeVo result = recommendationService.getHomeRecommendations(7L, 2);

        assertEquals("PERSONALIZED", result.getSource());
        assertEquals(20L, result.getBooks().getFirst().getId());
        verify(bookCategoryRepository).findByBookIdsWithCategory(List.of(10L, 20L, 30L));
        verify(bookReviewRepository).findAverageRatingsByBookIds(List.of(20L, 30L), 1);
    }

    @Test
    void defersInvalidationUntilTheSurroundingTransactionCommits() {
        stubPopularRecommendation();
        recommendationService.getHomeRecommendations(7L, 12);

        TransactionSynchronizationManager.initSynchronization();
        try {
            recommendationService.invalidateAfterCommit(7L);
            recommendationService.getHomeRecommendations(7L, 12);
            assertRepositoryQueriedTimes(1);

            TransactionSynchronizationManager.getSynchronizations().forEach(
                    TransactionSynchronization::afterCommit);
            recommendationService.getHomeRecommendations(7L, 12);
            assertRepositoryQueriedTimes(2);
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    private Book book(Long id, Long salesCount) {
        return Book.builder()
                .id(id)
                .isbn("978000000000" + id)
                .title("图书" + id)
                .publisher(Publisher.builder().id(1L).name("出版社").build())
                .originalPrice(java.math.BigDecimal.TEN)
                .salePrice(java.math.BigDecimal.ONE)
                .stock(10)
                .status(BookStatus.ON_SALE)
                .salesCount(salesCount)
                .build();
    }

    private OrderItem orderItem(Book book, int quantity) {
        return OrderItem.builder().book(book).quantity(quantity).build();
    }

    private List<BookCategory> categories(Long categoryId) {
        return List.of(BookCategory.builder()
                .category(Category.builder().id(categoryId).name("分类" + categoryId).build())
                .build());
    }

    private BookCategory bookCategory(Book book, Long categoryId) {
        return BookCategory.builder()
                .book(book)
                .category(Category.builder().id(categoryId).name("分类" + categoryId).build())
                .build();
    }

    private void stubPopularRecommendation() {
        when(orderItemRepository.findCompletedByUserId(any(), any())).thenReturn(List.of());
        when(bookRepository.findByStatusAndStockGreaterThan(BookStatus.ON_SALE, 0))
                .thenReturn(List.of(book(1L, 1L)));
        when(bookReviewRepository.findByUser_IdAndStatus(7L, 1)).thenReturn(List.of());
        when(bookCategoryRepository.findByBookIdsWithCategory(any())).thenReturn(List.of());
        when(bookReviewRepository.findAverageRatingsByBookIds(any(), eq(1))).thenReturn(List.of());
    }

    private void assertRepositoryQueriedTimes(int times) {
        verify(orderItemRepository, times(times)).findCompletedByUserId(
                7L, com.example.demo.entity.OrderStatus.COMPLETED);
    }
    @Test
    void returnsRequestedRecommendationPageAndHasMoreFlag() {
        when(orderItemRepository.findCompletedByUserId(any(), any())).thenReturn(List.of());
        when(bookRepository.findByStatusAndStockGreaterThan(BookStatus.ON_SALE, 0))
                .thenReturn(List.of(book(1L, 30L), book(2L, 20L), book(3L, 10L)));
        when(bookReviewRepository.findByUser_IdAndStatus(7L, 1)).thenReturn(List.of());

        RecommendationHomeVo result = recommendationService.getHomeRecommendations(7L, 2, 2);

        assertEquals(2, result.getPage());
        assertEquals(2, result.getSize());
        assertEquals(1, result.getBooks().size());
        assertEquals(3L, result.getBooks().getFirst().getId());
        assertEquals(false, result.getHasMore());
    }
}



