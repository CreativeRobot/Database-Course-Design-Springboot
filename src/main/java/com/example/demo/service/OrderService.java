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
import com.example.demo.entity.PaymentStatus;
import com.example.demo.entity.User;
import com.example.demo.entity.UserAddress;
import com.example.demo.repository.BookOrderRepository;
import com.example.demo.repository.BookRepository;
import com.example.demo.repository.CartItemRepository;
import com.example.demo.repository.InventoryLogRepository;
import com.example.demo.repository.OrderItemRepository;
import com.example.demo.repository.PaymentRepository;
import com.example.demo.repository.UserAddressRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.vo.OrderItemVo;
import com.example.demo.vo.OrderVo;
import com.example.demo.vo.PageVo;
import com.example.demo.vo.PaymentVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * 用户订单创建业务。
 * 订单、订单明细、库存扣减、库存流水和购物车清理位于同一事务中，任一步失败都会整体回滚。
 */
@Service
public class OrderService {

    private static final long PAYMENT_TIMEOUT_MINUTES = 30;
    private static final int MAX_ITEM_QUANTITY = 999;
    private static final BigDecimal MAX_DATABASE_AMOUNT = new BigDecimal("99999999.99");
    private static final DateTimeFormatter ORDER_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
    private static final DateTimeFormatter PAYMENT_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    @Autowired
    private BookOrderRepository bookOrderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private InventoryLogRepository inventoryLogRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private UserAddressRepository userAddressRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private InventoryService inventoryService;

    // ==================== 订单写操作 ====================

    /**
     * 使用当前用户购物车中已选商品创建待支付订单。
     * 库存通过条件更新原子扣减，成功后写入订单明细和 ORDER_OUT 库存流水。
     */
    @Transactional
    public OrderVo createOrder(Long userId, CreateOrderDTO dto) {
        if (dto == null || dto.getAddressId() == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "收货地址不能为空");
        }

        getActiveUser(userId);
        AddressSnapshot address = buildAddressSnapshot(getOwnedAddress(userId, dto.getAddressId()));
        List<CartSelection> selections = loadSelectedCart(userId);
        LocalDateTime createTime = LocalDateTime.now();

        // 固定加锁顺序，降低两个订单同时购买多本相同图书时发生死锁的概率。
        selections.sort(Comparator.comparing(CartSelection::bookId));

        List<DeductedLine> deductedLines = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (CartSelection selection : selections) {
            DeductedLine line = deductStock(selection);
            deductedLines.add(line);
            totalAmount = totalAmount.add(line.subtotal());
            validateAmount(totalAmount, "订单总金额超出系统支持范围");
        }

        BookOrder order = BookOrder.builder()
                .orderNo(generateOrderNo())
                .user(userRepository.getReferenceById(userId))
                .status(OrderStatus.PENDING_PAYMENT)
                .totalAmount(totalAmount)
                .discountAmount(BigDecimal.ZERO)
                .shippingFee(BigDecimal.ZERO)
                .payableAmount(totalAmount)
                .expireTime(createTime.plusMinutes(PAYMENT_TIMEOUT_MINUTES))
                .receiverName(address.receiverName())
                .receiverPhone(address.receiverPhone())
                .receiverAddress(address.receiverAddress())
                .remark(trimToNull(dto.getRemark()))
                .build();
        order = bookOrderRepository.saveAndFlush(order);

        List<OrderItem> orderItems = saveOrderItems(order, deductedLines);
        saveInventoryLogs(order, orderItems, deductedLines);

        // 只删除本次结算时读取到的购物车项，避免误删并发请求中新选中的商品。
        cartItemRepository.deleteAllByIdInBatch(
                selections.stream().map(CartSelection::cartItemId).toList());

        recommendationService.invalidateAllAfterCommit();

        return toOrderVo(order, orderItems);
    }

    /**
     * 取消当前用户的待支付订单，逐项退回库存并写入 ORDER_CANCEL_RETURN 流水。
     * 条件更新订单状态可以防止重复取消或与支付请求并发时重复退库。
     */
    @Transactional
    public OrderVo cancelOrder(Long userId, Long orderId) {
        getActiveUser(userId);
        BookOrder order = getOwnedOrder(userId, orderId);
        requireOrderStatus(order, OrderStatus.PENDING_PAYMENT, "只有待支付订单可以取消");

        String orderNo = order.getOrderNo();
        int affectedRows = bookOrderRepository.cancelPendingOrder(
                orderId,
                userId,
                OrderStatus.PENDING_PAYMENT,
                OrderStatus.CANCELLED,
                LocalDateTime.now()
        );
        if (affectedRows == 0) {
            throwCurrentOrderState(userId, orderId, "订单状态已变化，无法取消");
        }

        inventoryService.returnStockAndWriteLogs(orderId, orderNo);

        recommendationService.invalidateAllAfterCommit();
        return loadOrderVo(orderId);
    }

    /** 模拟订单支付成功，并将订单由待支付推进到待发货。 */
    @Transactional
    public int cancelExpiredOrders(LocalDateTime now) {
        LocalDateTime cancellationTime = now == null ? LocalDateTime.now() : now;
        List<BookOrder> expiredOrders =
                bookOrderRepository.findByStatusAndExpireTimeLessThanEqualOrderByExpireTimeAsc(
                        OrderStatus.PENDING_PAYMENT, cancellationTime);
        int cancelledCount = 0;
        for (BookOrder order : expiredOrders) {
            Long userId = order.getUser().getId();
            int affectedRows = bookOrderRepository.cancelPendingOrder(
                    order.getId(),
                    userId,
                    OrderStatus.PENDING_PAYMENT,
                    OrderStatus.CANCELLED,
                    cancellationTime
            );
            if (affectedRows == 0) {
                continue;
            }
            inventoryService.returnStockAndWriteLogs(order.getId(), order.getOrderNo());
            cancelledCount++;
        }
        if (cancelledCount > 0) {
            recommendationService.invalidateAllAfterCommit();
        }
        return cancelledCount;
    }

    @Transactional
    public PaymentVo payOrder(Long userId, Long orderId, PayOrderDTO dto) {
        if (dto == null || dto.getPaymentMethod() == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "支付方式不能为空");
        }

        getActiveUser(userId);
        BookOrder order = getOwnedOrder(userId, orderId);
        requireOrderStatus(order, OrderStatus.PENDING_PAYMENT, "只有待支付订单可以支付");

        LocalDateTime paidTime = LocalDateTime.now();
        int affectedRows = bookOrderRepository.payPendingOrder(
                orderId,
                userId,
                OrderStatus.PENDING_PAYMENT,
                OrderStatus.PENDING_SHIPMENT,
                paidTime
        );
        if (affectedRows == 0) {
            throwCurrentOrderState(userId, orderId, "订单状态已变化，无法支付");
        }

        Payment payment = Payment.builder()
                .paymentNo(generatePaymentNo())
                .order(bookOrderRepository.getReferenceById(orderId))
                .paymentMethod(dto.getPaymentMethod())
                .amount(order.getPayableAmount())
                .status(PaymentStatus.SUCCESS)
                .paidTime(paidTime)
                .build();
        return toPaymentVo(paymentRepository.saveAndFlush(payment));
    }

    /** 管理员将待发货订单推进到已发货状态。 */
    @Transactional
    public OrderVo shipOrder(Long orderId) {
        BookOrder order = getOrder(orderId);
        requireOrderStatus(order, OrderStatus.PENDING_SHIPMENT, "只有待发货订单可以发货");

        int affectedRows = bookOrderRepository.shipPendingOrder(
                orderId,
                OrderStatus.PENDING_SHIPMENT,
                OrderStatus.SHIPPED,
                LocalDateTime.now()
        );
        if (affectedRows == 0) {
            throwCurrentOrderState(orderId, "订单状态已变化，无法发货");
        }
        return loadOrderVo(orderId);
    }

    /** 当前用户确认收货，将已发货订单推进到已完成，完成后订单明细才允许评价。 */
    @Transactional
    public OrderVo completeOrder(Long userId, Long orderId) {
        getActiveUser(userId);
        BookOrder order = getOwnedOrder(userId, orderId);
        requireOrderStatus(order, OrderStatus.SHIPPED, "只有已发货订单可以确认收货");

        int affectedRows = bookOrderRepository.completeShippedOrder(
                orderId,
                userId,
                OrderStatus.SHIPPED,
                OrderStatus.COMPLETED,
                LocalDateTime.now()
        );
        if (affectedRows == 0) {
            throwCurrentOrderState(userId, orderId, "订单状态已变化，无法确认收货");
        }
        inventoryService.increaseSalesCount(
                orderItemRepository.findByOrder_IdOrderByIdAsc(orderId));

        recommendationService.invalidateAllAfterCommit();
        return loadOrderVo(orderId);
    }

    // ==================== 库存与订单明细 ====================

    /** 原子扣减一本图书的库存，并生成后续订单明细和流水所需的快照。 */
    @Transactional(readOnly = true)
    public PageVo<OrderVo> listUserOrders(
            Long userId, OrderStatus status, int page, int size) {
        getActiveUser(userId);
        Pageable pageable = buildOrderPageable(page, size);
        Page<BookOrder> orders = status == null
                ? bookOrderRepository.findByUser_IdOrderByCreateTimeDesc(userId, pageable)
                : bookOrderRepository.findByUser_IdAndStatusOrderByCreateTimeDesc(
                        userId, status, pageable);
        return PageVo.of(orders.map(this::toOrderVoWithItems));
    }

    @Transactional(readOnly = true)
    public OrderVo getUserOrder(Long userId, Long orderId) {
        getActiveUser(userId);
        return toOrderVoWithItems(getOwnedOrder(userId, orderId));
    }

    @Transactional(readOnly = true)
    public PageVo<OrderVo> listAdminOrders(
            String orderNo, Long userId, OrderStatus status, int page, int size) {
        if (userId != null && userId <= 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "用户 ID 必须为正数");
        }
        Pageable pageable = buildOrderPageable(page, size);
        String normalizedOrderNo = StringUtils.hasText(orderNo)
                ? orderNo.trim() : null;
        Page<BookOrder> orders = bookOrderRepository.searchForAdmin(
                normalizedOrderNo, userId, status, pageable);
        return PageVo.of(orders.map(this::toOrderVoWithItems));
    }

    @Transactional(readOnly = true)
    public OrderVo getAdminOrder(Long orderId) {
        return toOrderVoWithItems(getOrder(orderId));
    }

    private DeductedLine deductStock(CartSelection selection) {
        int affectedRows = bookRepository.decreaseStock(
                selection.bookId(), selection.quantity(), BookStatus.ON_SALE);
        if (affectedRows == 0) {
            throwStockException(selection.bookId());
        }

        // UPDATE 持有行锁至事务结束，此处读取到的库存可用于生成准确的前后库存流水。
        Book book = bookRepository.findById(selection.bookId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "图书不存在"));
        int afterStock = book.getStock();
        int beforeStock = afterStock + selection.quantity();
        BigDecimal subtotal = book.getSalePrice()
                .multiply(BigDecimal.valueOf(selection.quantity()));
        validateAmount(subtotal, "图书《" + book.getTitle() + "》小计超出系统支持范围");

        return new DeductedLine(
                book.getId(),
                book.getTitle(),
                book.getIsbn(),
                book.getSalePrice(),
                selection.quantity(),
                subtotal,
                beforeStock,
                afterStock
        );
    }

    /** 保存下单时的图书、价格和数量快照。 */
    private List<OrderItem> saveOrderItems(BookOrder order, List<DeductedLine> lines) {
        List<OrderItem> items = lines.stream()
                .map(line -> OrderItem.builder()
                        .order(order)
                        .book(bookRepository.getReferenceById(line.bookId()))
                        .bookTitle(line.bookTitle())
                        .isbn(line.isbn())
                        .unitPrice(line.unitPrice())
                        .quantity(line.quantity())
                        .subtotal(line.subtotal())
                        .build())
                .toList();
        return orderItemRepository.saveAllAndFlush(items);
    }

    /** 为每一条订单明细写入对应的出库流水。 */
    private void saveInventoryLogs(
            BookOrder order, List<OrderItem> items, List<DeductedLine> lines) {
        List<InventoryLog> logs = new ArrayList<>();
        for (int index = 0; index < lines.size(); index++) {
            DeductedLine line = lines.get(index);
            logs.add(InventoryLog.builder()
                    .book(items.get(index).getBook())
                    .changeQuantity(-line.quantity())
                    .beforeStock(line.beforeStock())
                    .afterStock(line.afterStock())
                    .changeType(InventoryChangeType.ORDER_OUT)
                    .order(order)
                    .remark("订单" + order.getOrderNo() + "创建扣减库存")
                    .build());
        }
        inventoryLogRepository.saveAllAndFlush(logs);
    }

    // ==================== 数据校验与转换 ====================

    /** 加载已选购物车商品，并在执行批量更新前转换为不受持久化上下文清理影响的快照。 */
    private List<CartSelection> loadSelectedCart(Long userId) {
        List<CartItem> cartItems = cartItemRepository
                .findByUser_IdAndSelectedTrueOrderByCreateTimeDesc(userId);
        if (cartItems.isEmpty()) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "请选择需要结算的商品");
        }

        List<CartSelection> selections = new ArrayList<>();
        for (CartItem item : cartItems) {
            if (item.getQuantity() == null
                    || item.getQuantity() < 1
                    || item.getQuantity() > MAX_ITEM_QUANTITY) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "购物车商品数量不正确");
            }
            selections.add(new CartSelection(
                    item.getId(), item.getBook().getId(), item.getQuantity()));
        }
        return selections;
    }

    /** 将用户地址转换为订单收货信息快照，避免后续修改地址影响历史订单。 */
    private AddressSnapshot buildAddressSnapshot(UserAddress address) {
        List<String> parts = new ArrayList<>();
        parts.add(address.getProvince().trim());
        parts.add(address.getCity().trim());
        if (StringUtils.hasText(address.getDistrict())) {
            parts.add(address.getDistrict().trim());
        }
        parts.add(address.getDetailAddress().trim());
        if (StringUtils.hasText(address.getPostalCode())) {
            parts.add("邮编:" + address.getPostalCode().trim());
        }

        String receiverAddress = String.join(" ", parts);
        if (receiverAddress.length() > 255) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "收货地址过长，无法保存订单快照");
        }
        return new AddressSnapshot(
                address.getReceiverName(), address.getReceiverPhone(), receiverAddress);
    }

    /** 库存扣减失败后区分图书不存在、下架和库存不足三种场景。 */
    private void throwStockException(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "图书不存在"));
        if (book.getStatus() != BookStatus.ON_SALE) {
            throw new BusinessException(HttpStatus.CONFLICT,
                    "图书《" + book.getTitle() + "》已下架");
        }
        throw new BusinessException(HttpStatus.CONFLICT,
                "图书《" + book.getTitle() + "》库存不足，当前库存为" + book.getStock());
    }

    private User getActiveUser(Long userId) {
        if (userId == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "未登录或Token已失效");
        }
        return userRepository.findByIdAndStatus(userId, 1)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND, "用户不存在或已被禁用"));
    }

    private UserAddress getOwnedAddress(Long userId, Long addressId) {
        return userAddressRepository.findByIdAndUser_Id(addressId, userId)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND, "收货地址不存在"));
    }

    private BookOrder getOwnedOrder(Long userId, Long orderId) {
        if (orderId == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "订单不能为空");
        }
        return bookOrderRepository.findByIdAndUser_Id(orderId, userId)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND, "订单不存在"));
    }

    private BookOrder getOrder(Long orderId) {
        if (orderId == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "订单不能为空");
        }
        return bookOrderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND, "订单不存在"));
    }

    private void requireOrderStatus(
            BookOrder order, OrderStatus expectedStatus, String message) {
        if (order.getStatus() != expectedStatus) {
            throw new BusinessException(HttpStatus.CONFLICT, message);
        }
    }

    private void throwCurrentOrderState(
            Long userId, Long orderId, String fallbackMessage) {
        BookOrder current = getOwnedOrder(userId, orderId);
        throw new BusinessException(HttpStatus.CONFLICT,
                fallbackMessage + "，当前状态为" + current.getStatus());
    }

    private void throwCurrentOrderState(Long orderId, String fallbackMessage) {
        BookOrder current = getOrder(orderId);
        throw new BusinessException(HttpStatus.CONFLICT,
                fallbackMessage + "，当前状态为" + current.getStatus());
    }

    private void validateAmount(BigDecimal amount, String message) {
        if (amount.compareTo(MAX_DATABASE_AMOUNT) > 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, message);
        }
    }

    /** 生成32位订单号：业务前缀、毫秒时间和随机串。 */
    private String generateOrderNo() {
        for (int attempt = 0; attempt < 5; attempt++) {
            String randomPart = UUID.randomUUID()
                    .toString()
                    .replace("-", "")
                    .substring(0, 13)
                    .toUpperCase();
            String orderNo = "BS"
                    + LocalDateTime.now().format(ORDER_TIME_FORMATTER)
                    + randomPart;
            if (!bookOrderRepository.existsByOrderNo(orderNo)) {
                return orderNo;
            }
        }
        throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "订单编号生成失败");
    }

    /** 生成支付流水号。 */
    private String generatePaymentNo() {
        for (int attempt = 0; attempt < 5; attempt++) {
            String randomPart = UUID.randomUUID()
                    .toString()
                    .replace("-", "")
                    .substring(0, 16)
                    .toUpperCase();
            String paymentNo = "PAY"
                    + LocalDateTime.now().format(PAYMENT_TIME_FORMATTER)
                    + randomPart;
            if (!paymentRepository.existsByPaymentNo(paymentNo)) {
                return paymentNo;
            }
        }
        throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "支付流水号生成失败");
    }

    private OrderVo loadOrderVo(Long orderId) {
        BookOrder order = getOrder(orderId);
        List<OrderItem> items = orderItemRepository.findByOrder_IdOrderByIdAsc(orderId);
        return toOrderVo(order, items);
    }

    private OrderVo toOrderVoWithItems(BookOrder order) {
        return toOrderVo(
                order,
                orderItemRepository.findByOrder_IdOrderByIdAsc(order.getId()));
    }

    private Pageable buildOrderPageable(int page, int size) {
        if (page < 1) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "页码必须从 1 开始");
        }
        if (size < 1 || size > 100) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST, "每页数量必须在 1 到 100 之间");
        }
        return PageRequest.of(
                page - 1,
                size,
                Sort.by(
                        Sort.Order.desc("createTime"),
                        Sort.Order.desc("id")));
    }

    private OrderVo toOrderVo(BookOrder order, List<OrderItem> items) {
        OrderVo vo = new OrderVo();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setStatus(order.getStatus());
        vo.setTotalAmount(order.getTotalAmount());
        vo.setDiscountAmount(order.getDiscountAmount());
        vo.setShippingFee(order.getShippingFee());
        vo.setPayableAmount(order.getPayableAmount());
        vo.setExpireTime(order.getExpireTime());
        vo.setReceiverName(order.getReceiverName());
        vo.setReceiverPhone(order.getReceiverPhone());
        vo.setReceiverAddress(order.getReceiverAddress());
        vo.setRemark(order.getRemark());
        vo.setCreateTime(order.getCreateTime());
        vo.setUpdateTime(order.getUpdateTime());
        vo.setPaidTime(order.getPaidTime());
        vo.setShippedTime(order.getShippedTime());
        vo.setCompletedTime(order.getCompletedTime());
        vo.setCancelledTime(order.getCancelledTime());
        vo.setItems(items.stream().map(this::toOrderItemVo).toList());
        return vo;
    }

    private PaymentVo toPaymentVo(Payment payment) {
        PaymentVo vo = new PaymentVo();
        vo.setId(payment.getId());
        vo.setPaymentNo(payment.getPaymentNo());
        vo.setOrderId(payment.getOrder().getId());
        vo.setOrderNo(payment.getOrder().getOrderNo());
        vo.setPaymentMethod(payment.getPaymentMethod());
        vo.setAmount(payment.getAmount());
        vo.setStatus(payment.getStatus());
        vo.setPaidTime(payment.getPaidTime());
        vo.setCreateTime(payment.getCreateTime());
        return vo;
    }

    private OrderItemVo toOrderItemVo(OrderItem item) {
        OrderItemVo vo = new OrderItemVo();
        vo.setId(item.getId());
        vo.setBookId(item.getBook().getId());
        vo.setBookTitle(item.getBookTitle());
        vo.setIsbn(item.getIsbn());
        vo.setUnitPrice(item.getUnitPrice());
        vo.setQuantity(item.getQuantity());
        vo.setSubtotal(item.getSubtotal());
        return vo;
    }

    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private record CartSelection(Long cartItemId, Long bookId, Integer quantity) {
    }

    private record DeductedLine(
            Long bookId,
            String bookTitle,
            String isbn,
            BigDecimal unitPrice,
            Integer quantity,
            BigDecimal subtotal,
            Integer beforeStock,
            Integer afterStock) {
    }

    private record AddressSnapshot(
            String receiverName, String receiverPhone, String receiverAddress) {
    }


}
