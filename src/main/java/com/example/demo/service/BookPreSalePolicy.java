package com.example.demo.service;

import com.example.demo.common.exception.BusinessException;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

/** Central rules for configuring and evaluating book pre-sales. */
public final class BookPreSalePolicy {
    private BookPreSalePolicy() {}

    public static boolean isActive(Boolean preSale, LocalDateTime releaseTime, LocalDateTime now) {
        LocalDateTime reference = now == null ? LocalDateTime.now() : now;
        return Boolean.TRUE.equals(preSale) && releaseTime != null && releaseTime.isAfter(reference);
    }

    public static void validateConfiguration(Boolean preSale, LocalDateTime releaseTime, LocalDateTime now) {
        if (!Boolean.TRUE.equals(preSale)) return;
        if (releaseTime == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "开启预售时必须设置预计发售时间");
        }
        LocalDateTime reference = now == null ? LocalDateTime.now() : now;
        if (!releaseTime.isAfter(reference)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "预计发售时间必须晚于当前时间");
        }
    }
}
