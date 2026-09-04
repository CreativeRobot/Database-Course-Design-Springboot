package com.example.demo.service;

import com.example.demo.common.exception.BusinessException;
import com.example.demo.dto.CreateBundleRefundRequestDTO;
import com.example.demo.dto.ReviewRefundDTO;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BundleRefundServiceTests {
    @Mock private BundleRefundRequestRepository bundleRefundRequestRepository;
    @Mock private BundleRefundRequestItemRepository bundleRefundRequestItemRepository;
    @Mock private BookOrderRepository bookOrderRepository;
    @Mock private OrderBundleApplicationRepository orderBundleApplicationRepository;
    @Mock private OrderBundleApplicationItemRepository orderBundleApplicationItemRepository;
    @Mock private OrderItemRepository orderItemRepository;
    @Mock private BookRepository bookRepository;
    @Mock private PaymentRepository paymentRepository;
    @Mock private InventoryLogRepository inventoryLogRepository;
    @Mock private RefundRequestRepository refundRequestRepository;
    @InjectMocks private BundleRefundService bundleRefundService;

    @Test
    void createsOnePendingAtomicRequestWithServerGeneratedSnapshotAndTailAdjustedAmount() {
        BookOrder order = paidOrder();
        OrderBundleApplication application = OrderBundleApplication.builder().id(31L).order(order)
                .bundleId(8L).bundleName("Java组合包").bundlePrice(new BigDecimal("29.99"))
                .regularAmount(new BigDecimal("35.00")).discountAmount(new BigDecimal("5.01")).build();
        OrderItem a = item(order, 11L, 101L, "A", new BigDecimal("20.00"), 1);
        OrderItem b = item(order, 12L, 102L, "B", new BigDecimal("15.00"), 1);
        OrderBundleApplicationItem ai = bundleItem(application, a, 101L, "A", new BigDecimal("20.00"), new BigDecimal("3.00"), 1);
        OrderBundleApplicationItem bi = bundleItem(application, b, 102L, "B", new BigDecimal("15.00"), new BigDecimal("2.01"), 1);
        when(bookOrderRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(order));
        when(orderBundleApplicationRepository.findByIdForUpdate(31L)).thenReturn(Optional.of(application));
        when(orderBundleApplicationItemRepository.findByApplication_IdOrderByIdAsc(31L)).thenReturn(List.of(ai, bi));
        when(orderItemRepository.findByIdForUpdate(11L)).thenReturn(Optional.of(a));
        when(orderItemRepository.findByIdForUpdate(12L)).thenReturn(Optional.of(b));
        when(refundRequestRepository.existsLegacyActiveForOrderItem(11L)).thenReturn(false);
        when(refundRequestRepository.existsLegacyActiveForOrderItem(12L)).thenReturn(false);
        when(bundleRefundRequestRepository.findByApplicationIdAndStatusIn(eq(31L), any())).thenReturn(List.of());
        when(bundleRefundRequestRepository.save(any(BundleRefundRequest.class))).thenAnswer(i -> i.getArgument(0));

        CreateBundleRefundRequestDTO dto = new CreateBundleRefundRequestDTO();
        dto.setBundleApplicationId(31L); dto.setType(RefundType.REFUND_ONLY); dto.setReason("整包退款");

        BundleRefundRequest result = bundleRefundService.createRequest(7L, 7L, dto);

        assertEquals(RefundStatus.PENDING, result.getStatus());
        assertEquals(new BigDecimal("29.99"), result.getAmount());
        assertEquals(2, result.getItems().size());
        assertEquals(new BigDecimal("29.99"), result.getItems().stream().map(BundleRefundRequestItem::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        assertSame(result, result.getItems().get(0).getRequest());
        verify(bundleRefundRequestRepository).save(any(BundleRefundRequest.class));
    }

    @Test
    void rejectsBundleApplicationWhenLegacyStandaloneRefundIsActive() {
        BookOrder order = paidOrder();
        OrderBundleApplication application = OrderBundleApplication.builder().id(31L).order(order)
                .bundleId(8L).bundleName("包").bundlePrice(new BigDecimal("10.00"))
                .regularAmount(new BigDecimal("10.00")).discountAmount(BigDecimal.ZERO).build();
        OrderItem item = item(order, 11L, 101L, "A", new BigDecimal("10.00"), 1);
        OrderBundleApplicationItem snapshot = bundleItem(application, item, 101L, "A", new BigDecimal("10.00"), BigDecimal.ZERO, 1);
        when(bookOrderRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(order));
        when(orderBundleApplicationRepository.findByIdForUpdate(31L)).thenReturn(Optional.of(application));
        when(orderBundleApplicationItemRepository.findByApplication_IdOrderByIdAsc(31L)).thenReturn(List.of(snapshot));
        when(orderItemRepository.findByIdForUpdate(11L)).thenReturn(Optional.of(item));
        when(refundRequestRepository.existsLegacyActiveForOrderItem(11L)).thenReturn(true);
        when(bundleRefundRequestRepository.findByApplicationIdAndStatusIn(eq(31L), any())).thenReturn(List.of());

        CreateBundleRefundRequestDTO dto = new CreateBundleRefundRequestDTO();
        dto.setBundleApplicationId(31L); dto.setType(RefundType.REFUND_ONLY); dto.setReason("冲突");

        BusinessException exception = assertThrows(BusinessException.class,
                () -> bundleRefundService.createRequest(7L, 7L, dto));
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        verify(bundleRefundRequestRepository, never()).save(any());
    }

    @Test
    void approvesAllBundleItemsAndRestoresEveryBookForReturnRefund() {
        BookOrder order = paidOrder();
        Book aBook = Book.builder().id(101L).stock(2).build();
        Book bBook = Book.builder().id(102L).stock(3).build();
        OrderItem a = item(order, 11L, 101L, "A", new BigDecimal("20.00"), 1); a.setBook(aBook);
        OrderItem b = item(order, 12L, 102L, "B", new BigDecimal("15.00"), 1); b.setBook(bBook);
        BundleRefundRequest request = BundleRefundRequest.builder().id(41L).refundNo("BR001").order(order)
                .bundleApplication(OrderBundleApplication.builder().id(31L).order(order).build())
                .user(order.getUser()).type(RefundType.RETURN_REFUND).status(RefundStatus.PENDING)
                .amount(new BigDecimal("29.99")).build();
        BundleRefundRequestItem ai = BundleRefundRequestItem.builder().id(1L).request(request).orderItem(a).bookId(101L)
                .bookTitle("A").isbn("A").salePrice(new BigDecimal("20.00")).allocatedDiscount(new BigDecimal("3.00"))
                .quantity(1).amount(new BigDecimal("17.00")).build();
        BundleRefundRequestItem bi = BundleRefundRequestItem.builder().id(2L).request(request).orderItem(b).bookId(102L)
                .bookTitle("B").isbn("B").salePrice(new BigDecimal("15.00")).allocatedDiscount(new BigDecimal("2.01"))
                .quantity(1).amount(new BigDecimal("12.99")).build();
        when(bundleRefundRequestRepository.findByIdForUpdate(41L)).thenReturn(Optional.of(request));
        when(bundleRefundRequestItemRepository.findByRequest_IdOrderByOrderItem_IdAsc(41L)).thenReturn(List.of(ai, bi));
        when(bookOrderRepository.findByIdForUpdate(7L)).thenReturn(Optional.of(order));
        when(orderBundleApplicationRepository.findByIdForUpdate(31L)).thenReturn(Optional.of(request.getBundleApplication()));
        when(orderItemRepository.findByIdForUpdate(11L)).thenReturn(Optional.of(a));
        when(orderItemRepository.findByIdForUpdate(12L)).thenReturn(Optional.of(b));
        when(bookRepository.findStockSnapshotForUpdate(101L)).thenReturn(Optional.of(stock(101L, 2)));
        when(bookRepository.findStockSnapshotForUpdate(102L)).thenReturn(Optional.of(stock(102L, 3)));
        when(bookRepository.increaseStock(101L, 1)).thenReturn(1);
        when(bookRepository.increaseStock(102L, 1)).thenReturn(1);
        when(bundleRefundRequestRepository.save(any(BundleRefundRequest.class))).thenAnswer(i -> i.getArgument(0));
        ReviewRefundDTO dto = new ReviewRefundDTO(); dto.setApproved(true); dto.setRemark("整包通过");

        BundleRefundRequest result = bundleRefundService.review(41L, 99L, dto);

        assertEquals(RefundStatus.APPROVED, result.getStatus());
        assertEquals(1, a.getRefundedQuantity());
        assertEquals(1, b.getRefundedQuantity());
        assertEquals(new BigDecimal("29.99"), order.getRefundedAmount());
        verify(bookRepository).increaseStock(101L, 1);
        verify(bookRepository).increaseStock(102L, 1);
        verify(inventoryLogRepository, times(2)).save(any(InventoryLog.class));
    }

    @Test
    void sameDirectionReviewIsIdempotentAndOppositeDirectionConflicts() {
        BundleRefundRequest request = BundleRefundRequest.builder().id(41L).status(RefundStatus.APPROVED).build();
        when(bundleRefundRequestRepository.findByIdForUpdate(41L)).thenReturn(Optional.of(request));
        ReviewRefundDTO same = new ReviewRefundDTO(); same.setApproved(true);
        assertSame(request, bundleRefundService.review(41L, 99L, same));
        ReviewRefundDTO opposite = new ReviewRefundDTO(); opposite.setApproved(false);
        BusinessException exception = assertThrows(BusinessException.class,
                () -> bundleRefundService.review(41L, 99L, opposite));
        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        verifyNoInteractions(bookOrderRepository, bookRepository, inventoryLogRepository);
    }

    private BookOrder paidOrder() {
        return BookOrder.builder().id(7L).orderNo("O7").user(User.builder().id(7L).username("customer").build())
                .status(OrderStatus.COMPLETED).payableAmount(new BigDecimal("100.00")).refundedAmount(BigDecimal.ZERO).build();
    }
    private OrderItem item(BookOrder order, Long id, Long bookId, String title, BigDecimal price, int quantity) {
        return OrderItem.builder().id(id).order(order).book(Book.builder().id(bookId).build()).bookTitle(title).isbn(title)
                .unitPrice(price).quantity(quantity).refundedQuantity(0).subtotal(price.multiply(BigDecimal.valueOf(quantity)))
                .discountAmount(BigDecimal.ZERO).paidSubtotal(price.multiply(BigDecimal.valueOf(quantity))).build();
    }
    private OrderBundleApplicationItem bundleItem(OrderBundleApplication app, OrderItem item, Long bookId, String title,
                                                    BigDecimal salePrice, BigDecimal discount, int quantity) {
        return OrderBundleApplicationItem.builder().id(item.getId()).application(app).orderItem(item).bookId(bookId)
                .bookTitle(title).isbn(title).salePrice(salePrice).allocatedDiscount(discount).quantity(quantity).build();
    }
    private BookStockSnapshot stock(Long id, int stock) {
        return new BookStockSnapshot() {
            public Long getId() { return id; }
            public Integer getStock() { return stock; }
            public String getStatus() { return BookStatus.ON_SALE.name(); }
            public java.math.BigDecimal getSalePrice() { return BigDecimal.ZERO; }
            public String getTitle() { return "book"; }
            public String getIsbn() { return "isbn"; }
            public Boolean getPreSale() { return false; }
            public java.time.LocalDateTime getPreSaleReleaseTime() { return null; }
        };
    }
}
