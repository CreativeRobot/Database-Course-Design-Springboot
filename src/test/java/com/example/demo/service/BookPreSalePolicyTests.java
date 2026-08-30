package com.example.demo.service;

import com.example.demo.common.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BookPreSalePolicyTests {

    private static final LocalDateTime NOW = LocalDateTime.of(2026, 8, 30, 12, 0);

    @Test
    void preSaleIsActiveOnlyBeforeConfiguredReleaseTime() {
        assertTrue(BookPreSalePolicy.isActive(
                true, LocalDateTime.of(2026, 9, 15, 10, 0), NOW));
        assertFalse(BookPreSalePolicy.isActive(
                true, LocalDateTime.of(2026, 8, 30, 11, 59), NOW));
        assertFalse(BookPreSalePolicy.isActive(
                false, LocalDateTime.of(2026, 9, 15, 10, 0), NOW));
    }

    @Test
    void enablingPreSaleRequiresAFutureReleaseTime() {
        BusinessException missing = assertThrows(BusinessException.class,
                () -> BookPreSalePolicy.validateConfiguration(true, null, NOW));
        assertEquals(HttpStatus.BAD_REQUEST, missing.getStatus());

        BusinessException past = assertThrows(BusinessException.class,
                () -> BookPreSalePolicy.validateConfiguration(
                        true, LocalDateTime.of(2026, 8, 30, 11, 59), NOW));
        assertEquals(HttpStatus.BAD_REQUEST, past.getStatus());
    }

    @Test
    void normalSaleDoesNotRequireAReleaseTime() {
        BookPreSalePolicy.validateConfiguration(false, null, NOW);
    }
}
