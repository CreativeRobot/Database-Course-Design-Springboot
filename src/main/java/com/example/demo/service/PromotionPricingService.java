package com.example.demo.service;

import com.example.demo.entity.BookPromotionStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Service
public class PromotionPricingService {
    public boolean isActive(BookPromotionStatus status, Integer discountPercent,
                            LocalDateTime startTime, LocalDateTime endTime, LocalDateTime now) {
        return status == BookPromotionStatus.ACTIVE
                && discountPercent != null && discountPercent > 0 && discountPercent < 100
                && startTime != null && endTime != null && now != null
                && !endTime.isBefore(startTime)
                && !now.isBefore(startTime) && now.isBefore(endTime);
    }

    public BigDecimal effectivePrice(BigDecimal basePrice, BookPromotionStatus status, Integer discountPercent,
                                     LocalDateTime startTime, LocalDateTime endTime, LocalDateTime now) {
        if (basePrice == null || !isActive(status, discountPercent, startTime, endTime, now)) {
            return money(basePrice);
        }
        return basePrice.multiply(BigDecimal.valueOf(discountPercent))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    public BigDecimal money(BigDecimal amount) {
        return amount == null ? null : amount.setScale(2, RoundingMode.HALF_UP);
    }
}
