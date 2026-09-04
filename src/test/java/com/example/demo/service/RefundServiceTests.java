package com.example.demo.service;

import com.example.demo.common.exception.BusinessException;
import com.example.demo.dto.CreateRefundRequestDTO;
import com.example.demo.dto.ReviewRefundDTO;
import com.example.demo.entity.Book;
import com.example.demo.entity.BookOrder;
import com.example.demo.entity.OrderItem;
import com.example.demo.entity.OrderStatus;
import com.example.demo.entity.RefundRequest;
import com.example.demo.entity.RefundStatus;
import com.example.demo.entity.RefundType;
import com.example.demo.entity.User;
import com.example.demo.repository.BookOrderRepository;
import com.example.demo.repository.BookRepository;
import com.example.demo.repository.InventoryLogRepository;
import com.example.demo.repository.OrderItemRepository;
import com.example.demo.repository.PaymentRepository;
import com.example.demo.repository.RefundRequestRepository;
import com.example.demo.vo.RefundRequestVo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RefundServiceTests {

    @Mock private RefundRequestRepository refundRequestRepository;
    @Mock private BookOrderRepository bookOrderRepository;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private BookRepository bookRepository;
    @Mock private PaymentRepository paymentRepository;
    @Mock private InventoryLogRepository inventoryLogRepository;
    @Mock private RefundAvailabilityService refundAvailabilityService;

    @InjectMocks private RefundService refundService;

    @Test
    void adminDetailUsesReadOnlyLookupAndReturnsApplicationReason() {
        BookOrder order = paidOrder();
        Book book = Book.builder().id(9L).build();
        OrderItem item = OrderItem.builder().id(11L).order(order).book(book)
                .bookTitle("Database Systems").quantity(2).refundedQuantity(0).build();
        RefundRequest request = RefundRequest.builder().id(21L).refundNo("REF001")
                .order(order).orderItem(item).user(order.getUser())
                .type(RefundType.RETURN_REFUND).status(RefundStatus.PENDING)
                .quantity(1).amount(new BigDecimal("12.50")).reason("图书破损").build();
        when(refundRequestRepository.findById(21L)).thenReturn(Optional.of(request));

        RefundRequestVo detail = refundService.getAdmin(21L);

        assertEquals("图书破损", detail.getReason());
        verify(refundRequestRepository).findById(21L);
        verify(refundRequestRepository, never()).findByIdForUpdate(21L);
    }
    @Test
    void calculatesRefundAmountOnServerAndCreatesPendingRequest() {
        BookOrder order = paidOrder();
        Book book = Book.builder().id(9L).stock(2).build();
        OrderItem item = OrderItem.builder().id(11L).order(order).book(book)
                .unitPrice(new BigDecimal("12.50")).quantity(2)
                .subtotal(new BigDecimal("25.00")).build();
        when(bookOrderRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(order));
        when(orderItemRepository.findByIdForUpdate(11L)).thenReturn(Optional.of(item));
        when(refundAvailabilityService.forItem(item)).thenReturn(new RefundAvailability(0, 2, 0, 0, 2));
        when(refundRequestRepository.save(any(RefundRequest.class))).thenAnswer(i -> i.getArgument(0));

        CreateRefundRequestDTO dto = new CreateRefundRequestDTO();
        dto.setOrderId(7L);
        dto.setOrderItemId(11L);
        dto.setType(RefundType.RETURN_REFUND);
        dto.setQuantity(1);
        dto.setReason("图书破损");

        RefundRequest result = refundService.createRequest(7L, dto);

        assertEquals(new BigDecimal("12.50"), result.getAmount());
        assertEquals(RefundStatus.PENDING, result.getStatus());
        assertEquals(1, result.getQuantity());
        verify(refundRequestRepository).save(any(RefundRequest.class));
    }

    @Test
    void refundAmountUsesActualPaidSubtotalAfterBundleDiscount() {
        BookOrder order = paidOrder();
        Book book = Book.builder().id(9L).stock(2).build();
        OrderItem item = OrderItem.builder().id(11L).order(order).book(book)
                .unitPrice(new BigDecimal("12.50")).quantity(2)
                .subtotal(new BigDecimal("25.00")).discountAmount(new BigDecimal("5.00"))
                .paidSubtotal(new BigDecimal("20.00")).build();
        when(bookOrderRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(order));
        when(orderItemRepository.findByIdForUpdate(11L)).thenReturn(Optional.of(item));
        when(refundAvailabilityService.forItem(item)).thenReturn(new RefundAvailability(0, 2, 0, 0, 2));
        when(refundRequestRepository.save(any(RefundRequest.class))).thenAnswer(i -> i.getArgument(0));
        CreateRefundRequestDTO dto = new CreateRefundRequestDTO();
        dto.setOrderId(7L); dto.setOrderItemId(11L); dto.setType(RefundType.REFUND_ONLY);
        dto.setQuantity(1); dto.setReason("组合包退款");

        RefundRequest result = refundService.createRequest(7L, dto);

        assertEquals(new BigDecimal("12.50"), result.getAmount());
    }

    @Test
    void approvingReturnRefundAtomicallyRestoresStockAndMarksRequestApproved() {
        BookOrder order = paidOrder();
        Book book = Book.builder().id(9L).stock(2).build();
        OrderItem item = OrderItem.builder().id(11L).order(order).book(book)
                .unitPrice(new BigDecimal("12.50")).quantity(2)
                .subtotal(new BigDecimal("25.00")).build();
        RefundRequest request = RefundRequest.builder().id(21L).order(order).orderItem(item)
                .type(RefundType.RETURN_REFUND).status(RefundStatus.PENDING)
                .quantity(1).amount(new BigDecimal("12.50")).build();
        when(refundRequestRepository.findByIdForUpdate(21L)).thenReturn(Optional.of(request));
        when(bookOrderRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(order));
        when(orderItemRepository.findByIdForUpdate(11L)).thenReturn(Optional.of(item));
        when(refundRequestRepository.sumApprovedAmount(7L)).thenReturn(new BigDecimal("0.00"));
        when(bookRepository.findStockSnapshotForUpdate(9L)).thenReturn(Optional.of(
                org.mockito.Mockito.mock(com.example.demo.repository.BookStockSnapshot.class)));
        when(bookRepository.increaseStock(9L, 1)).thenReturn(1);
        when(refundRequestRepository.save(any(RefundRequest.class))).thenAnswer(i -> i.getArgument(0));

        ReviewRefundDTO dto = new ReviewRefundDTO();
        dto.setApproved(true);
        dto.setRemark("同意退货退款");

        RefundRequest result = refundService.review(21L, 99L, dto);

        assertEquals(RefundStatus.APPROVED, result.getStatus());
        assertEquals(1, item.getRefundedQuantity());
        assertEquals(new BigDecimal("12.50"), order.getRefundedAmount());
        verify(bookRepository).increaseStock(9L, 1);
        verify(inventoryLogRepository).save(any());
    }

    @Test
    void rejectsDuplicateReviewWithoutChangingInventory() {
        RefundRequest request = RefundRequest.builder().id(21L).status(RefundStatus.APPROVED).build();
        when(refundRequestRepository.findByIdForUpdate(21L)).thenReturn(Optional.of(request));

        BusinessException exception = assertThrows(BusinessException.class,
                () -> refundService.review(21L, 99L, new ReviewRefundDTO()));

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        verify(bookRepository, never()).increaseStock(any(), any());
        verify(inventoryLogRepository, never()).save(any());
    }

    @Test
    void onlyOrderOwnerCanCreateRefundRequest() {
        BookOrder order = paidOrder();
        when(bookOrderRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(order));
        CreateRefundRequestDTO dto = new CreateRefundRequestDTO();
        dto.setOrderId(7L);
        dto.setOrderItemId(11L);
        dto.setType(RefundType.REFUND_ONLY);
        dto.setQuantity(1);

        BusinessException exception = assertThrows(BusinessException.class,
                () -> refundService.createRequest(88L, dto));

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
    }


    @Test
    void rejectsOrderItemThatBelongsToAnotherOrderBeforeCreatingRefund() {
        BookOrder order = paidOrder();
        BookOrder anotherOrder = BookOrder.builder().id(10L).build();
        OrderItem item = OrderItem.builder().id(11L).order(anotherOrder).quantity(2).build();
        when(bookOrderRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(order));
        when(orderItemRepository.findByIdForUpdate(11L)).thenReturn(Optional.of(item));

        CreateRefundRequestDTO dto = new CreateRefundRequestDTO();
        dto.setOrderId(7L);
        dto.setOrderItemId(11L);
        dto.setType(RefundType.REFUND_ONLY);
        dto.setQuantity(1);
        dto.setReason("归属校验");

        BusinessException exception = assertThrows(BusinessException.class,
                () -> refundService.createRequest(7L, dto));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verify(refundRequestRepository, never()).save(any());
    }

    @Test
    void standaloneRefundUsesUnbundledQuantityAndUnitPrice() {
        BookOrder order = paidOrder();
        Book book = Book.builder().id(9L).stock(2).build();
        OrderItem item = OrderItem.builder().id(11L).order(order).book(book)
                .unitPrice(new BigDecimal("12.50")).quantity(2)
                .subtotal(new BigDecimal("25.00")).discountAmount(new BigDecimal("5.00"))
                .paidSubtotal(new BigDecimal("20.00")).build();
        when(bookOrderRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(order));
        when(orderItemRepository.findByIdForUpdate(11L)).thenReturn(Optional.of(item));
        when(refundAvailabilityService.forItem(item)).thenReturn(new RefundAvailability(1, 1, 0, 0, 1));
        when(refundRequestRepository.save(any(RefundRequest.class))).thenAnswer(i -> i.getArgument(0));

        CreateRefundRequestDTO dto = new CreateRefundRequestDTO();
        dto.setOrderId(7L); dto.setOrderItemId(11L); dto.setType(RefundType.REFUND_ONLY);
        dto.setQuantity(1); dto.setReason("组合外商品退款");

        RefundRequest result = refundService.createRequest(7L, dto);

        assertEquals(new BigDecimal("12.50"), result.getAmount());
        assertEquals(Boolean.TRUE, result.getBundleAware());
        verify(refundRequestRepository).save(any(RefundRequest.class));
    }
    private BookOrder paidOrder() {
        return BookOrder.builder().id(7L).orderNo("BS202608280001").user(User.builder().id(7L).username("customer").build())
                .status(OrderStatus.COMPLETED).payableAmount(new BigDecimal("25.00"))
                .refundedAmount(BigDecimal.ZERO).build();
    }
}










