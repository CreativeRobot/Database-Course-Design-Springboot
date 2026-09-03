package com.example.demo.service;

import java.util.Comparator;
import java.util.List;

/**
 * RecommendationRanker 业务服务，封装相关领域的业务规则和数据访问流程。
 */
final class RecommendationRanker {
    private static final String CATEGORY_REASON = "与你喜欢的分类相似";
    private static final String CO_PURCHASE_REASON = "与已购图书常被一起购买";
    private static final String POPULAR_REASON = "热门畅销";

    private RecommendationRanker() {
    }

    // ==================== 业务方法 ====================

    /**
     * 执行当前模块的业务处理逻辑。
     */
    static List<RankedCandidate> rank(
            List<Candidate> candidates, boolean personalized, int limit) {
        Comparator<Candidate> comparator = personalized
                ? Comparator.comparingInt(Candidate::personalScore).reversed()
                : Comparator.comparingLong(Candidate::popularity).reversed();

        comparator = comparator
                .thenComparing(Comparator.comparingLong(Candidate::popularity).reversed())
                .thenComparing(Comparator.comparingDouble(Candidate::averageRating).reversed())
                .thenComparing(Comparator.comparingLong(Candidate::createdOrder).reversed())
                .thenComparingLong(Candidate::bookId);

        return candidates.stream()
                .sorted(comparator)
                .limit(limit)
                .map(candidate -> new RankedCandidate(candidate.bookId(),
                        personalized ? candidate.personalReason() : POPULAR_REASON))
                .toList();
    }

    record Candidate(
            Long bookId,
            int categoryScore,
            int coPurchaseScore,
            long popularity,
            double averageRating,
            long createdOrder) {

        int personalScore() {
            return categoryScore + coPurchaseScore;
        }

        String personalReason() {
            return coPurchaseScore > categoryScore ? CO_PURCHASE_REASON : CATEGORY_REASON;
        }
    }

    record RankedCandidate(Long bookId, String reason) {
    }
}
