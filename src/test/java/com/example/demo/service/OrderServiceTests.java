package com.example.demo.service;

import com.example.demo.common.exception.BusinessException;
import com.example.demo.dto.CreateOrderDTO;
import com.example.demo.dto.PayOrderDTO;
import com.example.demo.entity.Book;
import com.example.demo.entity.BookOrder;
import com.example.demo.entity.BookStatus;
import com.example.demo.entity.CartItem;
import com.example.demo.entity.InventoryChangeType;
import com.example.demo.entity.InventoryLog;
import com.example.demo.entity.OrderItem;
import com.example.demo.entity.OrderStatus;
import com.example.demo.entity.Payment;
import com.example.demo.entity.PaymentMethod;
import com.example.demo.entity.PaymentStatus;
import com.example.demo.entity.User;
import com.example.demo.entity.UserAddress;
import com.example.demo.repository.BookOrderRepository;
import com.example.demo.repository.BookRepository;
import com.example.demo.repository.BookStockSnapshot;
import com.example.demo.repository.CartItemRepository;
import com.example.demo.repository.InventoryLogRepository;
import com.example.demo.repository.OrderItemRepository;
import com.example.demo.repository.PaymentRepository;
import com.example.demo.repository.UserAddressRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.vo.OrderVo;
import com.example.demo.vo.PageVo;
import com.example.demo.vo.PaymentVo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTests {

    @Mock
    private BookOrderRepository bookOrderRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private InventoryLogRepository inventoryLogRepository;
    @Mock
    private PaymentRepository paymentRepository;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private UserAddressRepository userAddressRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private RecommendationService recommendationService;
    @Mock
    private InventoryService inventoryService;

    @InjectMocks
    private OrderService orderService;

    private User user;
    private UserAddress address;
    private CreateOrderDTO dto;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("buyer").status(1).build();
        address = UserAddress.builder()
                .id(20L)
                .user(user)
                .receiverName("张三")
                .receiverPhone("13800000000")
                .province("广东省")
                .city("深圳市")
                .district("南山区")
                .detailAddress("科技园1号")
                .defaultAddress(true)
                .build();
        dto = new CreateOrderDTO();
        dto.setAddressId(20L);

        lenient().when(userRepository.findByIdAndStatus(1L, 1)).thenReturn(Optional.of(user));
        lenient().when(userAddressRepository.findByIdAndUser_Id(20L, 1L))
                .thenReturn(Optional.of(address));
    }

    @Test
    void createOrderDeductsStockAndWritesInventoryLog() {
        Book cartBook = Book.builder().id(10L).stock(5).build();
        CartItem cartItem = CartItem.builder()
                .id(100L)
                .user(user)
                .book(cartBook)
                .quantity(2)
                .selected(true)
                .build();
        Book refreshedBook = Book.builder()
                .id(10L)
                .title("数据库系统概论")
                .isbn("9780000000001")
                .salePrice(new BigDecimal("20.00"))
                .stock(3)
                .status(BookStatus.ON_SALE)
                .build();

        when(cartItemRepository.findByUser_IdAndSelectedTrueOrderByCreateTimeDesc(1L))
                .thenReturn(List.of(cartItem));
        BookStockSnapshot snapshot = stockSnapshot(10L, 5, "数据库系统概论",
                "9780000000001", new BigDecimal("20.00"), BookStatus.ON_SALE);
        when(bookRepository.findStockSnapshotForUpdate(10L)).thenReturn(Optional.of(snapshot));
        when(bookRepository.decreaseStock(10L, 2, BookStatus.ON_SALE)).thenReturn(1);
        when(bookOrderRepository.existsByOrderNo(anyString())).thenReturn(false);
        when(userRepository.getReferenceById(1L)).thenReturn(user);
        when(bookRepository.getReferenceById(10L)).thenReturn(refreshedBook);
        when(bookOrderRepository.saveAndFlush(any(BookOrder.class))).thenAnswer(invocation -> {
            BookOrder order = invocation.getArgument(0);
            order.setId(30L);
            order.setCreateTime(LocalDateTime.now());
            return order;
        });
        when(orderItemRepository.saveAllAndFlush(any())).thenAnswer(invocation -> {
            List<OrderItem> items = invocation.getArgument(0);
            items.getFirst().setId(40L);
            return items;
        });

        OrderVo result = orderService.createOrder(1L, dto);

        assertEquals(new BigDecimal("40.00"), result.getTotalAmount());
        assertEquals(1, result.getItems().size());
        assertEquals(3, refreshedBook.getStock());
        verify(bookRepository).decreaseStock(10L, 2, BookStatus.ON_SALE);
        verify(inventoryLogRepository).saveAllAndFlush(argThat(logs -> {
            InventoryLog log = logs.iterator().next();
            return log.getChangeType() == InventoryChangeType.ORDER_OUT
                    && log.getChangeQuantity() == -2
                    && log.getBeforeStock() == 5
                    && log.getAfterStock() == 3;
        }));
        verify(cartItemRepository).deleteAllByIdInBatch(List.of(100L));
    }

    @Test
    void createOrderStopsBeforeSavingWhenStockIsInsufficient() {
        Book cartBook = Book.builder().id(10L).stock(1).build();
        CartItem cartItem = CartItem.builder()
                .id(100L)
                .user(user)
                .book(cartBook)
                .quantity(2)
                .selected(true)
                .build();
        when(cartItemRepository.findByUser_IdAndSelectedTrueOrderByCreateTimeDesc(1L))
                .thenReturn(List.of(cartItem));
        BookStockSnapshot snapshot = stockSnapshot(10L, 1, "数据库系统概论",
                "9780000000001", new BigDecimal("20.00"), BookStatus.ON_SALE);
        when(bookRepository.findStockSnapshotForUpdate(10L)).thenReturn(Optional.of(snapshot));

        BusinessException exception = assertThrows(
                BusinessException.class, () -> orderService.createOrder(1L, dto));

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        verify(bookOrderRepository, never()).saveAndFlush(any());
        verify(inventoryLogRepository, never()).saveAllAndFlush(any());
        verify(cartItemRepository, never()).deleteAllByIdInBatch(any());
    }

    private BookStockSnapshot stockSnapshot(
            Long id, int stock, String title, String isbn, BigDecimal salePrice, BookStatus status) {
        BookStockSnapshot snapshot = mock(BookStockSnapshot.class);
        lenient().when(snapshot.getId()).thenReturn(id);
        lenient().when(snapshot.getStock()).thenReturn(stock);
        lenient().when(snapshot.getTitle()).thenReturn(title);
        lenient().when(snapshot.getIsbn()).thenReturn(isbn);
        lenient().when(snapshot.getSalePrice()).thenReturn(salePrice);
        lenient().when(snapshot.getStatus()).thenReturn(status.name());
        return snapshot;
    }

    @Test
    void cancelOrderReturnsStockAndWritesReturnLog() {
        BookOrder pendingOrder = BookOrder.builder()
                .id(30L)
                .orderNo("BS202608050001")
                .user(user)
                .status(OrderStatus.PENDING_PAYMENT)
                .totalAmount(new BigDecimal("40.00"))
                .payableAmount(new BigDecimal("40.00"))
                .receiverName("张三")
                .receiverPhone("13800000000")
                .receiverAddress("广东省 深圳市 南山区 科技园1号")
                .build();
        BookOrder cancelledOrder = BookOrder.builder()
                .id(30L)
                .orderNo(pendingOrder.getOrderNo())
                .user(user)
                .status(OrderStatus.CANCELLED)
                .totalAmount(pendingOrder.getTotalAmount())
                .discountAmount(BigDecimal.ZERO)
                .shippingFee(BigDecimal.ZERO)
                .payableAmount(pendingOrder.getPayableAmount())
                .receiverName(pendingOrder.getReceiverName())
                .receiverPhone(pendingOrder.getReceiverPhone())
                .receiverAddress(pendingOrder.getReceiverAddress())
                .build();
        Book book = Book.builder().id(10L).stock(5).build();
        OrderItem item = OrderItem.builder()
                .id(40L)
                .order(pendingOrder)
                .book(book)
                .bookTitle("数据库系统概论")
                .isbn("9780000000001")
                .unitPrice(new BigDecimal("20.00"))
                .quantity(2)
                .subtotal(new BigDecimal("40.00"))
                .build();

        when(bookOrderRepository.findByIdAndUser_Id(30L, 1L))
                .thenReturn(Optional.of(pendingOrder));
        when(bookOrderRepository.cancelPendingOrder(
                org.mockito.ArgumentMatchers.eq(30L),
                org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.eq(OrderStatus.PENDING_PAYMENT),
                org.mockito.ArgumentMatchers.eq(OrderStatus.CANCELLED),
                any(LocalDateTime.class))).thenReturn(1);
        when(orderItemRepository.findByOrder_IdOrderByIdAsc(30L))
                .thenReturn(List.of(item));

        when(bookOrderRepository.findById(30L)).thenReturn(Optional.of(cancelledOrder));

        OrderVo result = orderService.cancelOrder(1L, 30L);

        assertEquals(OrderStatus.CANCELLED, result.getStatus());
        verify(inventoryService).returnStockAndWriteLogs(30L, pendingOrder.getOrderNo());

    }

    @Test
    void payOrderCreatesSuccessfulPayment() {
        BookOrder pendingOrder = BookOrder.builder()
                .id(30L)
                .orderNo("BS202608050001")
                .user(user)
                .status(OrderStatus.PENDING_PAYMENT)
                .payableAmount(new BigDecimal("40.00"))
                .build();
        PayOrderDTO payDto = new PayOrderDTO();
        payDto.setPaymentMethod(PaymentMethod.MOCK);

        when(bookOrderRepository.findByIdAndUser_Id(30L, 1L))
                .thenReturn(Optional.of(pendingOrder));
        when(bookOrderRepository.payPendingOrder(
                org.mockito.ArgumentMatchers.eq(30L),
                org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.eq(OrderStatus.PENDING_PAYMENT),
                org.mockito.ArgumentMatchers.eq(OrderStatus.PENDING_SHIPMENT),
                any(LocalDateTime.class))).thenReturn(1);
        when(paymentRepository.existsByPaymentNo(anyString())).thenReturn(false);
        when(bookOrderRepository.getReferenceById(30L)).thenReturn(pendingOrder);
        when(paymentRepository.saveAndFlush(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setId(50L);
            payment.setCreateTime(LocalDateTime.now());
            return payment;
        });

        PaymentVo result = orderService.payOrder(1L, 30L, payDto);

        assertEquals(PaymentStatus.SUCCESS, result.getStatus());
        assertEquals(PaymentMethod.MOCK, result.getPaymentMethod());
        assertEquals(new BigDecimal("40.00"), result.getAmount());
        verify(bookOrderRepository).payPendingOrder(
                org.mockito.ArgumentMatchers.eq(30L),
                org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.eq(OrderStatus.PENDING_PAYMENT),
                org.mockito.ArgumentMatchers.eq(OrderStatus.PENDING_SHIPMENT),
                any(LocalDateTime.class));
    }

    @Test
    void payOrderRejectsExpiredPendingOrderWhenSchedulerHasNotRunYet() {
        BookOrder expiredOrder = BookOrder.builder()
                .id(30L)
                .user(user)
                .status(OrderStatus.PENDING_PAYMENT)
                .expireTime(LocalDateTime.of(2026, 8, 5, 17, 0))
                .payableAmount(new BigDecimal("40.00"))
                .build();
        PayOrderDTO payDto = new PayOrderDTO();
        payDto.setPaymentMethod(PaymentMethod.MOCK);

        when(bookOrderRepository.findByIdAndUser_Id(30L, 1L))
                .thenReturn(Optional.of(expiredOrder));
        when(bookOrderRepository.payPendingOrder(
                org.mockito.ArgumentMatchers.eq(30L),
                org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.eq(OrderStatus.PENDING_PAYMENT),
                org.mockito.ArgumentMatchers.eq(OrderStatus.PENDING_SHIPMENT),
                any(LocalDateTime.class))).thenReturn(0);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> orderService.payOrder(1L, 30L, payDto));

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        verify(paymentRepository, never()).saveAndFlush(any(Payment.class));
    }

    @Test
    void shipOrderMovesPendingShipmentOrderToShipped() {
        BookOrder pendingShipment = BookOrder.builder()
                .id(30L)
                .status(OrderStatus.PENDING_SHIPMENT)
                .build();
        BookOrder shipped = BookOrder.builder()
                .id(30L)
                .status(OrderStatus.SHIPPED)
                .build();

        when(bookOrderRepository.findById(30L))
                .thenReturn(Optional.of(pendingShipment), Optional.of(shipped));
        when(bookOrderRepository.shipPendingOrder(
                org.mockito.ArgumentMatchers.eq(30L),
                org.mockito.ArgumentMatchers.eq(OrderStatus.PENDING_SHIPMENT),
                org.mockito.ArgumentMatchers.eq(OrderStatus.SHIPPED),
                any(LocalDateTime.class))).thenReturn(1);
        when(orderItemRepository.findByOrder_IdOrderByIdAsc(30L)).thenReturn(List.of());

        OrderVo result = orderService.shipOrder(30L);

        assertEquals(OrderStatus.SHIPPED, result.getStatus());
    }

    @Test
    void completeOrderMovesShippedOrderToCompleted() {
        BookOrder shipped = BookOrder.builder()
                .id(30L)
                .user(user)
                .status(OrderStatus.SHIPPED)
                .build();
        BookOrder completed = BookOrder.builder()
                .id(30L)
                .user(user)
                .status(OrderStatus.COMPLETED)
                .build();

        when(bookOrderRepository.findByIdAndUser_Id(30L, 1L))
                .thenReturn(Optional.of(shipped));
        when(bookOrderRepository.completeShippedOrder(
                org.mockito.ArgumentMatchers.eq(30L),
                org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.eq(OrderStatus.SHIPPED),
                org.mockito.ArgumentMatchers.eq(OrderStatus.COMPLETED),
                any(LocalDateTime.class))).thenReturn(1);
        when(bookOrderRepository.findById(30L)).thenReturn(Optional.of(completed));
        when(orderItemRepository.findByOrder_IdOrderByIdAsc(30L)).thenReturn(List.of());

        OrderVo result = orderService.completeOrder(1L, 30L);

        assertEquals(OrderStatus.COMPLETED, result.getStatus());
        verify(recommendationService).invalidateAllAfterCommit();
        verify(inventoryService).increaseSalesCount(List.of());
    }

    @Test
    void listUserOrdersReturnsPagedOrdersWithItems() {
        BookOrder order = BookOrder.builder()
                .id(30L)
                .user(user)
                .status(OrderStatus.COMPLETED)
                .build();
        when(bookOrderRepository.findByUser_IdOrderByCreateTimeDesc(
                org.mockito.ArgumentMatchers.eq(1L),
                any(Pageable.class))).thenReturn(new PageImpl<>(List.of(order)));
        when(orderItemRepository.findByOrder_IdOrderByIdAsc(30L))
                .thenReturn(List.of());

        PageVo<OrderVo> result = orderService.listUserOrders(1L, null, 1, 10);

        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
        assertEquals(30L, result.getRecords().getFirst().getId());
        assertEquals(OrderStatus.COMPLETED, result.getRecords().getFirst().getStatus());
    }

    @Test
    void listAdminOrdersSupportsCombinedFilters() {
        BookOrder order = BookOrder.builder()
                .id(30L)
                .orderNo("BS202608050001")
                .user(user)
                .status(OrderStatus.PENDING_PAYMENT)
                .build();
        when(bookOrderRepository.searchForAdmin(
                org.mockito.ArgumentMatchers.eq("BS2026"),
                org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.eq(OrderStatus.PENDING_PAYMENT),
                any(Pageable.class))).thenReturn(new PageImpl<>(List.of(order)));
        when(orderItemRepository.findByOrder_IdOrderByIdAsc(30L))
                .thenReturn(List.of());

        PageVo<OrderVo> result = orderService.listAdminOrders(
                " BS2026 ", 1L, OrderStatus.PENDING_PAYMENT, 1, 10);

        assertEquals(1, result.getTotal());
        assertEquals("BS202608050001", result.getRecords().getFirst().getOrderNo());
        verify(bookOrderRepository).searchForAdmin(
                org.mockito.ArgumentMatchers.eq("BS2026"),
                org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.eq(OrderStatus.PENDING_PAYMENT),
                any(Pageable.class));
    }

    @Test
    void cancelExpiredOrdersReturnsStockOnlyAfterConditionalUpdateSucceeds() {
        BookOrder expiredOrder = BookOrder.builder()
                .id(30L)
                .orderNo("BS202608050001")
                .user(user)
                .status(OrderStatus.PENDING_PAYMENT)
                .build();
        Book book = Book.builder().id(10L).stock(5).build();
        OrderItem item = OrderItem.builder()
                .id(40L)
                .order(expiredOrder)
                .book(book)
                .quantity(2)
                .build();
        LocalDateTime now = LocalDateTime.of(2026, 8, 5, 18, 0);

        when(bookOrderRepository
                .findByStatusAndExpireTimeLessThanEqualOrderByExpireTimeAsc(
                        OrderStatus.PENDING_PAYMENT, now))
                .thenReturn(List.of(expiredOrder));
        when(bookOrderRepository.cancelPendingOrder(
                org.mockito.ArgumentMatchers.eq(30L),
                org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.eq(OrderStatus.PENDING_PAYMENT),
                org.mockito.ArgumentMatchers.eq(OrderStatus.CANCELLED),
                org.mockito.ArgumentMatchers.eq(now))).thenReturn(1);


        int result = orderService.cancelExpiredOrders(now);

        assertEquals(1, result);
        verify(inventoryService).returnStockAndWriteLogs(30L, expiredOrder.getOrderNo());
    }

    @Test
    void cancelExpiredOrdersSkipsOrderAlreadyPaidOrCancelled() {
        BookOrder expiredOrder = BookOrder.builder()
                .id(30L)
                .orderNo("BS202608050001")
                .user(user)
                .status(OrderStatus.PENDING_PAYMENT)
                .build();
        LocalDateTime now = LocalDateTime.of(2026, 8, 5, 18, 0);

        when(bookOrderRepository
                .findByStatusAndExpireTimeLessThanEqualOrderByExpireTimeAsc(
                        OrderStatus.PENDING_PAYMENT, now))
                .thenReturn(List.of(expiredOrder));
        when(bookOrderRepository.cancelPendingOrder(
                org.mockito.ArgumentMatchers.eq(30L),
                org.mockito.ArgumentMatchers.eq(1L),
                org.mockito.ArgumentMatchers.eq(OrderStatus.PENDING_PAYMENT),
                org.mockito.ArgumentMatchers.eq(OrderStatus.CANCELLED),
                org.mockito.ArgumentMatchers.eq(now))).thenReturn(0);

        int result = orderService.cancelExpiredOrders(now);

        assertEquals(0, result);
        verify(bookRepository, never()).increaseStock(any(Long.class), any(Integer.class));
        verify(inventoryLogRepository, never()).saveAllAndFlush(any());
    }
}
