package com.example.demo.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * OrderExpirationScheduler 业务服务，封装相关领域的业务规则和数据访问流程。
 */
@Component
public class OrderExpirationScheduler {

    @Autowired
    private OrderService orderService;

    // ==================== 业务方法 ====================

    /**
     * 校验请求参数并更新当前业务状态或数据。
     */
    @Scheduled(fixedDelayString = "${app.order.expiration-scan-ms:60000}")
    public void cancelExpiredOrders() {
        orderService.cancelExpiredOrders(LocalDateTime.now());
    }
}
