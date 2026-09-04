package com.example.demo.service;

import com.example.demo.common.exception.BusinessException;
import com.example.demo.dto.CreateBundleRefundRequestDTO;
import com.example.demo.dto.ReviewRefundDTO;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import com.example.demo.vo.BundleRefundRequestItemVo;
import com.example.demo.vo.BundleRefundRequestVo;
import com.example.demo.vo.PageVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
public class BundleRefundService {
    private static final int MAX_PAGE_SIZE = 100;

    @Autowired private BundleRefundRequestRepository bundleRefundRequestRepository;
    @Autowired private BundleRefundRequestItemRepository bundleRefundRequestItemRepository;
    @Autowired private BookOrderRepository bookOrderRepository;
    @Autowired private OrderBundleApplicationRepository orderBundleApplicationRepository;
    @Autowired private OrderBundleApplicationItemRepository orderBundleApplicationItemRepository;
    @Autowired private OrderItemRepository orderItemRepository;
    @Autowired private BookRepository bookRepository;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private InventoryLogRepository inventoryLogRepository;
    @Autowired private RefundRequestRepository refundRequestRepository;

    @Transactional
    public BundleRefundRequest createRequest(Long userId, Long orderId, CreateBundleRefundRequestDTO dto) {
        if (dto == null || dto.getBundleApplicationId() == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "组合应用不能为空");
        }
        if (!StringUtils.hasText(dto.getReason())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "售后原因不能为空");
        }
        BookOrder order = bookOrderRepository.findByIdForUpdate(orderId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "订单不存在"));
        assertOwnerAndPaid(order, userId);
        OrderBundleApplication application = orderBundleApplicationRepository.findByIdForUpdate(dto.getBundleApplicationId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "订单组合不存在"));
        if (application.getOrder() == null || !order.getId().equals(application.getOrder().getId())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "组合不属于该订单");
        }
        List<BundleRefundRequest> existing = bundleRefundRequestRepository.findByApplicationIdAndStatusIn(
                application.getId(), List.of(RefundStatus.PENDING, RefundStatus.APPROVED));
        if (!existing.isEmpty()) {
            throw new BusinessException(HttpStatus.CONFLICT, "该组合已有进行中或已通过的整包售后");
        }

        List<OrderBundleApplicationItem> snapshots = orderBundleApplicationItemRepository
                .findByApplication_IdOrderByIdAsc(application.getId());
        if (snapshots.isEmpty()) {
            throw new BusinessException(HttpStatus.CONFLICT, "组合没有可退款明细");
        }
        List<LockedBundleItem> lockedItems = new ArrayList<>();
        for (OrderBundleApplicationItem snapshot : snapshots.stream()
                .sorted(Comparator.comparing(OrderBundleApplicationItem::getId, Comparator.nullsLast(Long::compareTo))).toList()) {
            OrderItem item = orderItemRepository.findByIdForUpdate(snapshot.getOrderItem().getId())
                    .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "订单明细不存在"));
            if (item.getOrder() == null || !order.getId().equals(item.getOrder().getId())) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "组合明细不属于该订单");
            }
            if (refundRequestRepository.existsLegacyActiveForOrderItem(item.getId())) {
                throw new BusinessException(HttpStatus.CONFLICT, "组合成员存在历史普通售后，暂不可整包退款");
            }
            int refunded = zero(item.getRefundedQuantity());
            int quantity = positive(snapshot.getQuantity());
            if (refunded + quantity > positive(item.getQuantity())) {
                throw new BusinessException(HttpStatus.CONFLICT, "组合成员可退款数量已被占用");
            }
            lockedItems.add(new LockedBundleItem(snapshot, item));
        }

        BigDecimal total = money(application.getBundlePrice());
        List<BigDecimal> amounts = allocateAmounts(lockedItems, total);
        BundleRefundRequest request = BundleRefundRequest.builder()
                .refundNo(generateRefundNo())
                .order(order).bundleApplication(application).user(order.getUser())
                .type(dto.getType()).status(RefundStatus.PENDING).amount(total)
                .reason(dto.getReason().trim()).build();
        List<BundleRefundRequestItem> requestItems = new ArrayList<>();
        for (int i = 0; i < lockedItems.size(); i++) {
            LockedBundleItem locked = lockedItems.get(i);
            OrderBundleApplicationItem snapshot = locked.snapshot();
            OrderItem item = locked.item();
            requestItems.add(BundleRefundRequestItem.builder()
                    .request(request).orderItem(item)
                    .bookId(snapshot.getBookId()).bookTitle(snapshot.getBookTitle()).isbn(snapshot.getIsbn())
                    .salePrice(money(snapshot.getSalePrice())).allocatedDiscount(money(snapshot.getAllocatedDiscount()))
                    .quantity(snapshot.getQuantity()).amount(amounts.get(i)).build());
        }
        request.setItems(requestItems);
        return bundleRefundRequestRepository.save(request);
    }

    @Transactional
    public BundleRefundRequest review(Long refundId, Long adminId, ReviewRefundDTO dto) {
        BundleRefundRequest request = bundleRefundRequestRepository.findByIdForUpdate(refundId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "整包售后申请不存在"));
        if (dto == null || dto.getApproved() == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "必须指定审核结果");
        }
        RefundStatus requestedStatus = dto.getApproved() ? RefundStatus.APPROVED : RefundStatus.REJECTED;
        if (request.getStatus() != RefundStatus.PENDING) {
            if (request.getStatus() == requestedStatus) {
                return request;
            }
            throw new BusinessException(HttpStatus.CONFLICT, "该整包售后申请已经审核，不能反向处理");
        }

        BookOrder order = bookOrderRepository.findByIdForUpdate(request.getOrder().getId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "订单不存在"));
        OrderBundleApplication application = orderBundleApplicationRepository.findByIdForUpdate(
                        request.getBundleApplication().getId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "订单组合不存在"));
        List<BundleRefundRequestItem> requestItems = bundleRefundRequestItemRepository
                .findByRequest_IdOrderByOrderItem_IdAsc(request.getId());
        if (requestItems.isEmpty()) {
            throw new BusinessException(HttpStatus.CONFLICT, "整包售后没有明细");
        }
        List<LockedBundleRequestItem> lockedItems = new ArrayList<>();
        for (BundleRefundRequestItem requestItem : requestItems) {
            OrderItem item = orderItemRepository.findByIdForUpdate(requestItem.getOrderItem().getId())
                    .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "订单明细不存在"));
            if (item.getOrder() == null || !order.getId().equals(item.getOrder().getId())) {
                throw new BusinessException(HttpStatus.CONFLICT, "整包明细与订单不一致");
            }
            if (zero(item.getRefundedQuantity()) + positive(requestItem.getQuantity()) > positive(item.getQuantity())) {
                throw new BusinessException(HttpStatus.CONFLICT, "组合成员可退款数量已被其他售后占用");
            }
            lockedItems.add(new LockedBundleRequestItem(requestItem, item));
        }
        request.setReviewer(User.builder().id(adminId).build());
        request.setReviewedTime(LocalDateTime.now());
        request.setReviewRemark(trimToNull(dto.getRemark()));
        if (!dto.getApproved()) {
            request.setStatus(RefundStatus.REJECTED);
            return bundleRefundRequestRepository.save(request);
        }

        BigDecimal currentRefunded = money(order.getRefundedAmount());
        BigDecimal newTotal = currentRefunded.add(money(request.getAmount())).setScale(2, RoundingMode.HALF_UP);
        if (order.getPayableAmount() != null && newTotal.compareTo(order.getPayableAmount()) > 0) {
            throw new BusinessException(HttpStatus.CONFLICT, "累计退款金额超过订单实付金额");
        }
        if (request.getType() == RefundType.RETURN_REFUND) {
            for (LockedBundleRequestItem locked : lockedItems.stream()
                    .sorted(Comparator.comparing(i -> i.requestItem().getBookId())).toList()) {
                BundleRefundRequestItem requestItem = locked.requestItem();
                Book book = locked.item().getBook();
                BookStockSnapshot stock = bookRepository.findStockSnapshotForUpdate(book.getId())
                        .orElseThrow(() -> new BusinessException(HttpStatus.CONFLICT, "关联图书不存在"));
                int before = zero(stock.getStock());
                int quantity = positive(requestItem.getQuantity());
                if (bookRepository.increaseStock(book.getId(), quantity) != 1) {
                    throw new BusinessException(HttpStatus.CONFLICT, "库存回补失败");
                }
                inventoryLogRepository.save(InventoryLog.builder()
                        .book(book).order(order).changeQuantity(quantity)
                        .beforeStock(before).afterStock(before + quantity)
                        .changeType(InventoryChangeType.REFUND_RETURN)
                        .remark("整包售后" + request.getRefundNo() + "退货回补库存").build());
            }
        }
        for (LockedBundleRequestItem locked : lockedItems) {
            OrderItem item = locked.item();
            item.setRefundedQuantity(zero(item.getRefundedQuantity()) + positive(locked.requestItem().getQuantity()));
        }
        order.setRefundedAmount(newTotal);
        request.setStatus(RefundStatus.APPROVED);
        BundleRefundRequest saved = bundleRefundRequestRepository.save(request);
        if (order.getPayableAmount() != null && newTotal.compareTo(order.getPayableAmount()) == 0) {
            paymentRepository.findFirstByOrder_IdAndStatusOrderByCreateTimeDesc(order.getId(), PaymentStatus.SUCCESS)
                    .ifPresent(payment -> { payment.setStatus(PaymentStatus.REFUNDED); paymentRepository.save(payment); });
        }
        return saved;
    }

    @Transactional(readOnly = true)
    public PageVo<BundleRefundRequestVo> listUser(Long userId, int page, int size) {
        return PageVo.of(bundleRefundRequestRepository.findByUser_IdOrderByCreateTimeDesc(userId, pageRequest(page, size))
                .map(this::toVo));
    }

    @Transactional(readOnly = true)
    public BundleRefundRequestVo getUser(Long userId, Long id) {
        return toVo(bundleRefundRequestRepository.findByIdAndUser_Id(id, userId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "整包售后申请不存在")));
    }

    @Transactional(readOnly = true)
    public PageVo<BundleRefundRequestVo> listAdmin(RefundStatus status, RefundType type, int page, int size) {
        return PageVo.of(bundleRefundRequestRepository.searchForAdmin(status, type, pageRequest(page, size))
                .map(this::toVo));
    }

    @Transactional(readOnly = true)
    public BundleRefundRequestVo getAdmin(Long id) {
        return toVo(bundleRefundRequestRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "整包售后申请不存在")));
    }

    public BundleRefundRequestVo toVoForController(BundleRefundRequest request) { return toVo(request); }

    private List<BigDecimal> allocateAmounts(List<LockedBundleItem> items, BigDecimal total) {
        List<BigDecimal> result = new ArrayList<>();
        BigDecimal allocated = BigDecimal.ZERO.setScale(2);
        for (int i = 0; i < items.size(); i++) {
            LockedBundleItem locked = items.get(i);
            BigDecimal amount;
            if (i == items.size() - 1) {
                amount = total.subtract(allocated).setScale(2, RoundingMode.HALF_UP);
            } else {
                amount = money(locked.snapshot().getSalePrice())
                        .multiply(BigDecimal.valueOf(positive(locked.snapshot().getQuantity())))
                        .subtract(money(locked.snapshot().getAllocatedDiscount()))
                        .setScale(2, RoundingMode.HALF_UP);
                if (amount.signum() <= 0) {
                    throw new BusinessException(HttpStatus.CONFLICT, "组合退款明细金额无效");
                }
            }
            if (amount.signum() <= 0) {
                throw new BusinessException(HttpStatus.CONFLICT, "组合退款明细金额无效");
            }
            result.add(amount);
            allocated = allocated.add(amount);
        }
        if (!allocated.equals(total)) {
            throw new BusinessException(HttpStatus.CONFLICT, "组合退款金额分摊不一致");
        }
        return result;
    }

    private BundleRefundRequestVo toVo(BundleRefundRequest request) {
        BundleRefundRequestVo vo = new BundleRefundRequestVo();
        vo.setId(request.getId()); vo.setRefundNo(request.getRefundNo());
        BookOrder order = request.getOrder(); OrderBundleApplication application = request.getBundleApplication();
        vo.setOrderId(order.getId()); vo.setOrderNo(order.getOrderNo()); vo.setBundleApplicationId(application.getId());
        vo.setBundleId(application.getBundleId()); vo.setBundleName(application.getBundleName());
        vo.setType(request.getType()); vo.setStatus(request.getStatus()); vo.setAmount(request.getAmount());
        vo.setReason(request.getReason()); vo.setReviewRemark(request.getReviewRemark());
        if (request.getReviewer() != null) vo.setReviewerId(request.getReviewer().getId());
        vo.setReviewedTime(request.getReviewedTime()); vo.setCreateTime(request.getCreateTime());
        List<BundleRefundRequestItem> items = request.getId() == null ? request.getItems() :
                bundleRefundRequestItemRepository.findByRequest_IdOrderByIdAsc(request.getId());
        vo.setItems(items == null ? List.of() : items.stream().map(this::toItemVo).toList());
        return vo;
    }

    private BundleRefundRequestItemVo toItemVo(BundleRefundRequestItem item) {
        BundleRefundRequestItemVo vo = new BundleRefundRequestItemVo();
        vo.setOrderItemId(item.getOrderItem().getId()); vo.setBookId(item.getBookId()); vo.setBookTitle(item.getBookTitle());
        vo.setIsbn(item.getIsbn()); vo.setSalePrice(item.getSalePrice()); vo.setAllocatedDiscount(item.getAllocatedDiscount());
        vo.setQuantity(item.getQuantity()); vo.setAmount(item.getAmount()); return vo;
    }

    private void assertOwnerAndPaid(BookOrder order, Long userId) {
        if (order.getUser() == null || !userId.equals(order.getUser().getId())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "无权申请该订单售后");
        }
        if (order.getStatus() == OrderStatus.PENDING_PAYMENT || order.getStatus() == OrderStatus.CANCELLED) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "未支付订单不能申请退款");
        }
    }
    private PageRequest pageRequest(int page, int size) {
        if (page < 1 || size < 1 || size > MAX_PAGE_SIZE) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "页码必须从1开始，每页数量必须在1到100之间");
        }
        return PageRequest.of(page - 1, size, Sort.by(Sort.Order.desc("createTime"), Sort.Order.desc("id")));
    }
    private String generateRefundNo() { return "BR" + UUID.randomUUID().toString().replace("-", "").substring(0, 24).toUpperCase(); }
    private int positive(Integer value) { return value == null ? 0 : value; }
    private int zero(Integer value) { return value == null ? 0 : value; }
    private BigDecimal money(BigDecimal value) { return value == null ? BigDecimal.ZERO.setScale(2) : value.setScale(2, RoundingMode.HALF_UP); }
    private String trimToNull(String value) { return StringUtils.hasText(value) ? value.trim() : null; }
    private record LockedBundleItem(OrderBundleApplicationItem snapshot, OrderItem item) { }
    private record LockedBundleRequestItem(BundleRefundRequestItem requestItem, OrderItem item) { }
}
