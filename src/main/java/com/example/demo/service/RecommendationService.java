package com.example.demo.service;

import com.example.demo.common.exception.BusinessException;
import com.example.demo.entity.Book;
import com.example.demo.entity.BookCategory;
import com.example.demo.entity.BookReview;
import com.example.demo.entity.BookStatus;
import com.example.demo.entity.OrderItem;
import com.example.demo.entity.OrderStatus;
import com.example.demo.repository.BookCategoryRepository;
import com.example.demo.repository.BookRepository;
import com.example.demo.repository.BookReviewRepository;
import com.example.demo.repository.OrderItemRepository;
import com.example.demo.vo.RecommendationBookVo;
import com.example.demo.vo.RecommendationHomeVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

@Service
public class RecommendationService {
    private static final int ENABLED_REVIEW_STATUS = 1;
    private static final int MAX_LIMIT = 20;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookCategoryRepository bookCategoryRepository;

    @Autowired
    private BookReviewRepository bookReviewRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    private final Cache<CacheKey, RecommendationHomeVo> cache = Caffeine.newBuilder()
            .maximumSize(1_000)
            .expireAfterWrite(java.time.Duration.ofMinutes(15))
            .build();

    @Transactional(readOnly = true)
    public RecommendationHomeVo getHomeRecommendations(Long userId, int limit) {
        if (userId == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "未登录或Token已失效");
        }
        if (limit < 1 || limit > MAX_LIMIT) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "推荐数量必须在1到20之间");
        }

        CacheKey cacheKey = new CacheKey(userId, limit);
        RecommendationHomeVo cached = cache.getIfPresent(cacheKey);
        if (cached != null) {
            return cached;
        }

        RecommendationHomeVo response = buildRecommendations(userId, limit);
        cache.put(cacheKey, response);
        return response;
    }

    public void invalidate(Long userId) {
        if (userId != null) {
            cache.asMap().keySet().removeIf(cacheKey -> cacheKey.userId().equals(userId));
        }
    }

    public void invalidateAll() {
        cache.invalidateAll();
    }

    public void invalidateAllAfterCommit() {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            invalidateAll();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                invalidateAll();
            }
        });
    }

    public void invalidateAfterCommit(Long userId) {
        if (userId == null) {
            return;
        }
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            invalidate(userId);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                invalidate(userId);
            }
        });
    }

    private RecommendationHomeVo buildRecommendations(Long userId, int limit) {
        List<OrderItem> purchasedItems = orderItemRepository.findCompletedByUserId(
                userId, OrderStatus.COMPLETED);
        List<Long> purchasedBookIds = purchasedItems.stream()
                .map(item -> item.getBook().getId())
                .distinct()
                .toList();
        List<Book> candidates = purchasedBookIds.isEmpty()
                ? bookRepository.findByStatusAndStockGreaterThan(BookStatus.ON_SALE, 0)
                : bookRepository.findByStatusAndStockGreaterThanAndIdNotIn(
                        BookStatus.ON_SALE, 0, purchasedBookIds);
        if (candidates.isEmpty()) {
            return new RecommendationHomeVo("POPULAR", List.of());
        }

        List<Long> allBookIds = new java.util.ArrayList<>(purchasedBookIds);
        allBookIds.addAll(candidates.stream().map(Book::getId).toList());
        List<BookCategory> categoryRelations = bookCategoryRepository.findByBookIdsWithCategory(allBookIds);
        Map<Long, List<BookCategory>> categoriesByBook = (categoryRelations == null ? List.<BookCategory>of() : categoryRelations)
                .stream()
                .collect(Collectors.groupingBy(category -> category.getBook().getId()));
        List<Object[]> ratingRows = bookReviewRepository.findAverageRatingsByBookIds(
                candidates.stream().map(Book::getId).toList(), ENABLED_REVIEW_STATUS);
        Map<Long, Double> averageRatings = (ratingRows == null ? List.<Object[]>of() : ratingRows)
                .stream()
                .collect(Collectors.toMap(row -> ((Number) row[0]).longValue(),
                        row -> ((Number) row[1]).doubleValue()));
        Map<Long, Integer> categoryWeights = buildCategoryWeights(userId, purchasedItems, categoriesByBook);
        Map<Long, Integer> coPurchaseScores = buildCoPurchaseScores(purchasedBookIds);
        Map<Long, Book> booksById = new HashMap<>();
        List<RecommendationRanker.Candidate> rankerCandidates = candidates.stream()
                .map(book -> {
                    booksById.put(book.getId(), book);
                    return toCandidate(book, categoriesByBook, averageRatings, categoryWeights, coPurchaseScores);
                })
                .toList();
        boolean personalized = rankerCandidates.stream()
                .anyMatch(candidate -> candidate.personalScore() > 0);

        List<RecommendationBookVo> books = RecommendationRanker.rank(
                        rankerCandidates, personalized, limit)
                .stream()
                .map(ranked -> toVo(booksById.get(ranked.bookId()), ranked.reason()))
                .toList();
        return new RecommendationHomeVo(personalized ? "PERSONALIZED" : "POPULAR", books);
    }

    private Map<Long, Integer> buildCategoryWeights(
            Long userId, List<OrderItem> purchasedItems, Map<Long, List<BookCategory>> categoriesByBook) {
        Map<Long, Integer> weights = new HashMap<>();
        for (OrderItem item : purchasedItems) {
            addCategoryWeight(weights, categoriesByBook.getOrDefault(item.getBook().getId(), List.of()), 3);
        }
        for (BookReview review : bookReviewRepository.findByUser_IdAndStatus(userId, ENABLED_REVIEW_STATUS)) {
            addCategoryWeight(weights, categoriesByBook.getOrDefault(review.getBook().getId(), List.of()),
                    Math.max(0, review.getRating() - 1));
        }
        return weights;
    }

    private void addCategoryWeight(Map<Long, Integer> weights, List<BookCategory> categories, int weight) {
        if (weight <= 0) {
            return;
        }
        for (BookCategory category : categories) {
            weights.merge(category.getCategory().getId(), weight, Integer::sum);
        }
    }

    private Map<Long, Integer> buildCoPurchaseScores(List<Long> purchasedBookIds) {
        if (purchasedBookIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, Integer> scores = new HashMap<>();
        for (Object[] row : orderItemRepository.findCoPurchasedBookScores(
                purchasedBookIds, OrderStatus.COMPLETED)) {
            scores.put(((Number) row[0]).longValue(), ((Number) row[1]).intValue() * 2);
        }
        return scores;
    }

    private RecommendationRanker.Candidate toCandidate(
            Book book, Map<Long, List<BookCategory>> categoriesByBook,
            Map<Long, Double> averageRatings, Map<Long, Integer> categoryWeights,
            Map<Long, Integer> coPurchaseScores) {
        int categoryScore = categoriesByBook.getOrDefault(book.getId(), List.of())
                .stream()
                .mapToInt(relation -> categoryWeights.getOrDefault(
                        relation.getCategory().getId(), 0))
                .sum();
        Double averageRating = averageRatings.get(book.getId());
        long createdOrder = book.getCreateTime() == null ? book.getId()
                : book.getCreateTime().atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli();
        return new RecommendationRanker.Candidate(
                book.getId(),
                categoryScore,
                coPurchaseScores.getOrDefault(book.getId(), 0),
                book.getSalesCount() == null ? 0L : book.getSalesCount(),
                averageRating == null ? 0.0 : averageRating,
                createdOrder);
    }

    private RecommendationBookVo toVo(Book book, String reason) {
        RecommendationBookVo vo = new RecommendationBookVo();
        vo.setId(book.getId());
        vo.setIsbn(book.getIsbn());
        vo.setTitle(book.getTitle());
        vo.setPublisherId(book.getPublisher().getId());
        vo.setPublisherName(book.getPublisher().getName());
        vo.setOriginalPrice(book.getOriginalPrice());
        vo.setSalePrice(book.getSalePrice());
        vo.setStock(book.getStock());
        vo.setStatus(book.getStatus());
        vo.setCoverUrl(book.getCoverUrl());
        vo.setReason(reason);
        return vo;
    }

    private record CacheKey(Long userId, int limit) {
    }
}
