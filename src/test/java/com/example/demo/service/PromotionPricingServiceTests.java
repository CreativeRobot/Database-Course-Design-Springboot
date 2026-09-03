package com.example.demo.service;

import com.example.demo.entity.BookPromotionStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class PromotionPricingServiceTests {

    private final PromotionPricingService pricing = new PromotionPricingService();
    private final LocalDateTime now = LocalDateTime.of(2026, 9, 3, 12, 0);

    @Test
    void appliesActivePromotionInsideItsTimeWindow() {
        BigDecimal price = pricing.effectivePrice(
                money("88.80"), BookPromotionStatus.ACTIVE, 80,
                now.minusHours(1), now.plusHours(1), now);

        assertThat(price).isEqualByComparingTo("71.04");
    }

    @Test
    void ignoresInactiveOrExpiredPromotion() {
        assertThat(pricing.effectivePrice(money("88.80"), BookPromotionStatus.INACTIVE, 80,
                now.minusHours(1), now.plusHours(1), now)).isEqualByComparingTo("88.80");
        assertThat(pricing.effectivePrice(money("88.80"), BookPromotionStatus.ACTIVE, 80,
                now.minusDays(2), now.minusMinutes(1), now)).isEqualByComparingTo("88.80");
    }

    @Test
    void endsPromotionAtItsEndTimeExclusive() {
        assertThat(pricing.isActive(BookPromotionStatus.ACTIVE, 80, now.minusHours(1), now, now)).isFalse();
    }

    private static BigDecimal money(String value) {
        return new BigDecimal(value);
    }
}
