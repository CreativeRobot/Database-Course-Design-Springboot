package com.example.demo.service;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class BundlePricingServiceTests {

    private final BundlePricingService pricing = new BundlePricingService();

    @Test
    void appliesOneCompleteBundleOnlyOnceEvenWhenQuantityIsGreaterThanOne() {
        var result = pricing.price(
                Map.of(
                        1L, new BundlePricingService.CartBook(1L, 2, money("10.00")),
                        2L, new BundlePricingService.CartBook(2L, 1, money("20.00"))),
                List.of(new BundlePricingService.BundleCandidate(7L, "套装", money("25.00"), List.of(
                        new BundlePricingService.BundleMember(1L, "书1", null, money("10.00")),
                        new BundlePricingService.BundleMember(2L, "书2", null, money("20.00"))))));

        assertThat(result.selectedBundleIds()).containsExactly(7L);
        assertThat(result.regularAmount()).isEqualByComparingTo("40.00");
        assertThat(result.discountAmount()).isEqualByComparingTo("5.00");
        assertThat(result.payableAmount()).isEqualByComparingTo("35.00");
    }

    @Test
    void choosesGlobalMaximumSavingsInsteadOfGreedyFirstBundle() {
        var result = pricing.price(
                Map.of(
                        1L, new BundlePricingService.CartBook(1L, 1, money("10.00")),
                        2L, new BundlePricingService.CartBook(2L, 1, money("10.00")),
                        3L, new BundlePricingService.CartBook(3L, 1, money("10.00")),
                        4L, new BundlePricingService.CartBook(4L, 1, money("10.00"))),
                List.of(
                        bundle(1L, "贪心组合", "15.00", 1L, 2L),
                        bundle(2L, "整体组合", "14.00", 1L, 3L),
                        bundle(3L, "整体组合2", "14.00", 2L, 4L)));

        assertThat(result.selectedBundleIds()).containsExactly(2L, 3L);
        assertThat(result.discountAmount()).isEqualByComparingTo("12.00");
    }

    @Test
    void usesAscendingIdSequenceAsDeterministicTieBreaker() {
        var result = pricing.price(
                Map.of(
                        1L, new BundlePricingService.CartBook(1L, 1, money("10.00")),
                        2L, new BundlePricingService.CartBook(2L, 1, money("10.00")),
                        3L, new BundlePricingService.CartBook(3L, 1, money("10.00"))),
                List.of(
                        bundle(9L, "后序", "15.00", 1L, 2L),
                        bundle(4L, "前序", "15.00", 2L, 3L)));

        assertThat(result.selectedBundleIds()).containsExactly(4L);
    }

    @Test
    void ignoresIncompleteAndNonCheaperBundles() {
        var result = pricing.price(
                Map.of(1L, new BundlePricingService.CartBook(1L, 1, money("10.00"))),
                List.of(bundle(1L, "缺书", "1.00", 1L, 2L), bundle(2L, "不优惠", "10.00", 1L)));

        assertThat(result.eligibleBundleIds()).isEmpty();
        assertThat(result.discountAmount()).isEqualByComparingTo("0.00");
    }

    private static BundlePricingService.BundleCandidate bundle(
            long id, String name, String price, long... bookIds) {
        return new BundlePricingService.BundleCandidate(
                id,
                name,
                money(price),
                java.util.Arrays.stream(bookIds)
                        .mapToObj(bookId -> new BundlePricingService.BundleMember(
                                bookId, "书" + bookId, null, money("10.00")))
                        .toList());
    }

    private static BigDecimal money(String value) {
        return new BigDecimal(value);
    }
}



