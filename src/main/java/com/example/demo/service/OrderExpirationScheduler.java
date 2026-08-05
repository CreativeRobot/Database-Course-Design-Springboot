package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class OrderExpirationScheduler {

    @Autowired
    private OrderService orderService;

    @Scheduled(fixedDelayString = "${app.order.expiration-scan-ms:60000}")
    public void cancelExpiredOrders() {
        orderService.cancelExpiredOrders(LocalDateTime.now());
    }
}
