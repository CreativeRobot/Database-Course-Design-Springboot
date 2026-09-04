package com.example.demo.service;

import com.example.demo.entity.Book;
import com.example.demo.entity.BookOrder;
import com.example.demo.entity.OrderBundleApplicationItem;
import com.example.demo.entity.OrderItem;
import com.example.demo.entity.OrderStatus;
import com.example.demo.entity.RefundStatus;
import com.example.demo.entity.User;
import com.example.demo.repository.OrderBundleApplicationItemRepository;
import com.example.demo.repository.RefundRequestRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefundAvailabilityServiceTests {
    @Mock private RefundRequestRepository refundRequestRepository;
    @Mock private OrderBundleApplicationItemRepository bundleItemRepository;
    @InjectMocks private RefundAvailabilityService service;

    @Test
    void separatesBundleCoveredQuantityFromStandaloneRefundableQuantity() {
        BookOrder order = BookOrder.builder().id(7L).status(OrderStatus.COMPLETED)
                .user(User.builder().id(9L).build()).build();
        OrderItem item = OrderItem.builder().id(11L).order(order)
                .book(Book.builder().id(101L).build()).quantity(2).refundedQuantity(0).build();
        OrderBundleApplicationItem bundleItem = OrderBundleApplicationItem.builder()
                .orderItem(item).quantity(1).build();

        when(bundleItemRepository.findByOrderItem_IdOrderByIdAsc(11L)).thenReturn(List.of(bundleItem));
        when(refundRequestRepository.sumQuantityByOrderItemIdAndStatus(11L, RefundStatus.APPROVED)).thenReturn(0);
        when(refundRequestRepository.sumQuantityByOrderItemIdAndStatus(11L, RefundStatus.PENDING)).thenReturn(0);

        RefundAvailability availability = service.forItem(item);

        assertEquals(1, availability.bundleCoveredQuantity());
        assertEquals(1, availability.standaloneQuantity());
        assertEquals(1, availability.standaloneRefundableQuantity());
    }

    @Test
    void approvedQuantityIsNotSubtractedTwiceWhenOrderItemAlreadyTracksIt() {
        BookOrder order = BookOrder.builder().id(7L).status(OrderStatus.COMPLETED)
                .user(User.builder().id(9L).build()).build();
        OrderItem item = OrderItem.builder().id(12L).order(order)
                .book(Book.builder().id(102L).build()).quantity(2).refundedQuantity(1).build();

        when(bundleItemRepository.findByOrderItem_IdOrderByIdAsc(12L)).thenReturn(List.of());
        when(refundRequestRepository.sumQuantityByOrderItemIdAndStatus(12L, RefundStatus.APPROVED)).thenReturn(1);
        when(refundRequestRepository.sumQuantityByOrderItemIdAndStatus(12L, RefundStatus.PENDING)).thenReturn(0);

        RefundAvailability availability = service.forItem(item);

        assertEquals(1, availability.standaloneRefundableQuantity());
    }
}
