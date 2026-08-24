package com.example.demo.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RecommendationRankerTests {

    @Test
    void ranksCoPurchaseAffinityBeforeCategoryAffinityAndExplainsWhy() {
        RecommendationRanker.Candidate categoryMatch = new RecommendationRanker.Candidate(
                1L, 8, 0, 50L, 4.8, 1L);
        RecommendationRanker.Candidate coPurchaseMatch = new RecommendationRanker.Candidate(
                2L, 3, 10, 10L, 4.0, 2L);

        List<RecommendationRanker.RankedCandidate> ranked = RecommendationRanker.rank(
                List.of(categoryMatch, coPurchaseMatch), true, 2);

        assertEquals(2L, ranked.getFirst().bookId());
        assertEquals("与已购图书常被一起购买", ranked.getFirst().reason());
        assertEquals(1L, ranked.get(1).bookId());
        assertEquals("与你喜欢的分类相似", ranked.get(1).reason());
    }

    @Test
    void fallsBackToPopularityWhenCustomerHasNoPreferenceHistory() {
        RecommendationRanker.Candidate lessPopular = new RecommendationRanker.Candidate(
                1L, 0, 0, 12L, 4.9, 3L);
        RecommendationRanker.Candidate morePopular = new RecommendationRanker.Candidate(
                2L, 0, 0, 18L, 3.0, 2L);

        List<RecommendationRanker.RankedCandidate> ranked = RecommendationRanker.rank(
                List.of(lessPopular, morePopular), false, 1);

        assertEquals(2L, ranked.getFirst().bookId());
        assertEquals("热门畅销", ranked.getFirst().reason());
    }
}
