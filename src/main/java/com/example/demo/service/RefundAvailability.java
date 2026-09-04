package com.example.demo.service;

public record RefundAvailability(
        int bundleCoveredQuantity,
        int standaloneQuantity,
        int approvedStandaloneQuantity,
        int pendingStandaloneQuantity,
        int standaloneRefundableQuantity) {
}
