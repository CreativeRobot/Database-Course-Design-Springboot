package com.example.demo.service;

import com.example.demo.common.exception.BusinessException;
import com.example.demo.dto.CreateRefundRequestDTO;
import com.example.demo.dto.ReviewRefundDTO;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import com.example.demo.vo.PageVo;
import com.example.demo.vo.RefundRequestVo;
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
import java.util.UUID;

/**
 * 退款业务服务，负责退款申请、审核和状态更新。
 */
@Service
public class RefundService {
    private static final int MAX_PAGE_SIZE = 100;
    @Autowired private RefundRequestRepository refundRequestRepository;
    @Autowired private BookOrderRepository bookOrderRepository;
    @Autowired private OrderItemRepository orderItemRepository;
    @Autowired private BookRepository bookRepository;
    @Autowired private PaymentRepository paymentRepository;
    @Autowired private InventoryLogRepository inventoryLogRepository;

    // ==================== 业务方法 ====================

    /**
     * 创建并保存当前业务数据。
     */
    @Transactional
    public RefundRequest createRequest(Long userId, CreateRefundRequestDTO dto) {
        BookOrder order = bookOrderRepository.findById(dto.getOrderId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "订单不存在"));
        if (order.getUser() == null || !userId.equals(order.getUser().getId())) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "无权申请该订单售后");
        }
        if (order.getStatus() == OrderStatus.PENDING_PAYMENT || order.getStatus() == OrderStatus.CANCELLED
                || order.getPaidTime() == null && order.getStatus() == OrderStatus.PENDING_PAYMENT) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "未支付订单不能申请退款");
        }
        OrderItem item = orderItemRepository.findByIdForUpdate(dto.getOrderItemId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "订单明细不存在"));
        int alreadyApplied = defaultZero(refundRequestRepository.sumApprovedOrPendingQuantity(item.getId()));
        int available = item.getQuantity() - defaultZero(item.getRefundedQuantity()) - alreadyApplied;
        if (dto.getQuantity() == null || dto.getQuantity() <= 0 || dto.getQuantity() > available) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "退款数量超过可售后数量");
        }
        if (!StringUtils.hasText(dto.getReason())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "售后原因不能为空");
        }
        BigDecimal paidSubtotal = item.getPaidSubtotal();
        BigDecimal discountAmount = defaultMoney(item.getDiscountAmount());
        BigDecimal amount;
        if (paidSubtotal != null && (paidSubtotal.signum() > 0 || discountAmount.signum() > 0)) {
            amount = paidSubtotal.multiply(BigDecimal.valueOf(dto.getQuantity()))
                    .divide(BigDecimal.valueOf(item.getQuantity()), 8, RoundingMode.HALF_UP)
                    .setScale(2, RoundingMode.HALF_UP);
        } else {
            // 兼容 V6 以前创建的历史订单：其 paid_subtotal 尚未保存。
            amount = item.getUnitPrice().multiply(BigDecimal.valueOf(dto.getQuantity()))
                    .setScale(2, RoundingMode.HALF_UP);
        }
        RefundRequest request = RefundRequest.builder()
                .refundNo(generateRefundNo())
                .order(order).orderItem(item).user(order.getUser())
                .type(dto.getType()).quantity(dto.getQuantity()).amount(amount)
                .reason(dto.getReason().trim()).status(RefundStatus.PENDING).build();
        return refundRequestRepository.save(request);
    }

    /**
     * 执行当前模块的业务处理逻辑。
     */
    @Transactional
    public RefundRequest review(Long refundId, Long adminId, ReviewRefundDTO dto) {
        RefundRequest request = refundRequestRepository.findByIdForUpdate(refundId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "售后申请不存在"));
        if (request.getStatus() != RefundStatus.PENDING) {
            throw new BusinessException(HttpStatus.CONFLICT, "该售后申请已经审核，不能重复处理");
        }
        if (dto.getApproved() == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "必须指定审核结果");
        }
        BookOrder order = bookOrderRepository.findByIdForUpdate(request.getOrder().getId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "订单不存在"));
        OrderItem item = orderItemRepository.findByIdForUpdate(request.getOrderItem().getId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "订单明细不存在"));
        LocalDateTime now = LocalDateTime.now();
        request.setReviewer(User.builder().id(adminId).build());
        request.setReviewedTime(now);
        request.setReviewRemark(trimToNull(dto.getRemark()));
        if (!dto.getApproved()) {
            request.setStatus(RefundStatus.REJECTED);
            return refundRequestRepository.save(request);
        }

        int refunded = defaultZero(item.getRefundedQuantity());
        if (refunded + request.getQuantity() > item.getQuantity()) {
            throw new BusinessException(HttpStatus.CONFLICT, "可退款数量已被其他售后申请占用");
        }
        BigDecimal alreadyRefunded = defaultMoney(refundRequestRepository.sumApprovedAmount(order.getId()));
        BigDecimal newTotal = alreadyRefunded.add(request.getAmount());
        if (newTotal.compareTo(order.getPayableAmount()) > 0) {
            throw new BusinessException(HttpStatus.CONFLICT, "累计退款金额超过订单实付金额");
        }

        if (request.getType() == RefundType.RETURN_REFUND) {
            Book book = item.getBook();
            var snapshot = bookRepository.findStockSnapshotForUpdate(book.getId())
                    .orElseThrow(() -> new BusinessException(HttpStatus.CONFLICT, "关联图书不存在"));
            int before = snapshot.getStock();
            int after = before + request.getQuantity();
            if (bookRepository.increaseStock(book.getId(), request.getQuantity()) != 1) {
                throw new BusinessException(HttpStatus.CONFLICT, "库存回补失败");
            }
            inventoryLogRepository.save(InventoryLog.builder()
                    .book(book).order(order).changeQuantity(request.getQuantity())
                    .beforeStock(before).afterStock(after)
                    .changeType(InventoryChangeType.REFUND_RETURN)
                    .remark("售后" + request.getRefundNo() + "退货回补库存").build());
        }
        item.setRefundedQuantity(refunded + request.getQuantity());
        order.setRefundedAmount(newTotal);
        request.setStatus(RefundStatus.APPROVED);
        RefundRequest saved = refundRequestRepository.save(request);
        if (newTotal.compareTo(order.getPayableAmount()) == 0) {
            paymentRepository.findFirstByOrder_IdAndStatusOrderByCreateTimeDesc(order.getId(), PaymentStatus.SUCCESS)
                    .ifPresent(payment -> { payment.setStatus(PaymentStatus.REFUNDED); paymentRepository.save(payment); });
        }
        return saved;
    }

    /**
     * 查询并返回当前模块所需的数据。
     */
    @Transactional(readOnly = true)
    public PageVo<RefundRequestVo> listAdmin(RefundStatus status, RefundType type, int page, int size) {
        PageRequest pageable = pageRequest(page, size);
        return PageVo.of(refundRequestRepository.searchForAdmin(status, type, pageable).map(this::toVo));
    }

    /**
     * 查询并返回当前模块所需的数据。
     */
    @Transactional(readOnly = true)
    public RefundRequestVo getAdmin(Long id) {
        return toVo(refundRequestRepository.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "售后申请不存在")));
    }

    /**
     * 查询并返回当前模块所需的数据。
     */
    @Transactional(readOnly = true)
    public PageVo<RefundRequestVo> listUser(Long userId, int page, int size) {
        PageRequest pageable = pageRequest(page, size);
        return PageVo.of(refundRequestRepository.findByUser_IdOrderByCreateTimeDesc(userId, pageable).map(this::toVo));
    }

    /**
     * 查询并返回当前模块所需的数据。
     */
    @Transactional(readOnly = true)
    public RefundRequestVo getUser(Long userId, Long id) {
        return toVo(refundRequestRepository.findByIdAndUser_Id(id, userId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "售后申请不存在")));
    }

    /**
     * 执行当前模块的辅助处理逻辑。
     */
    public RefundRequestVo toVoForController(RefundRequest r) { return toVo(r); }

    /**
     * 执行当前模块的业务处理逻辑。
     */
    /**
     * 执行当前模块的业务处理逻辑。
     */
    /**
     * 执行当前模块的业务处理逻辑。
     */
    /**
     * 执行当前模块的辅助处理逻辑。
     */
    /**
     * 查询并返回当前模块所需的数据。
     */
    /**
     * 执行当前模块的辅助处理逻辑。
     */
    private RefundRequestVo toVo(RefundRequest r) {
        RefundRequestVo vo = new RefundRequestVo();
        vo.setId(r.getId()); vo.setRefundNo(r.getRefundNo());
        BookOrder order = r.getOrder(); OrderItem item = r.getOrderItem();
        vo.setOrderId(order.getId()); vo.setOrderNo(order.getOrderNo());
        vo.setOrderItemId(item.getId()); vo.setUserId(r.getUser().getId()); vo.setUsername(r.getUser().getUsername());
        vo.setBookId(item.getBook().getId()); vo.setBookTitle(item.getBookTitle());
        vo.setType(r.getType()); vo.setStatus(r.getStatus()); vo.setQuantity(r.getQuantity());
        vo.setItemQuantity(item.getQuantity()); vo.setRefundedQuantity(item.getRefundedQuantity());
        vo.setAmount(r.getAmount()); vo.setReason(r.getReason()); vo.setReviewRemark(r.getReviewRemark());
        if (r.getReviewer() != null) vo.setReviewerId(r.getReviewer().getId());
        vo.setReviewedTime(r.getReviewedTime()); vo.setCreateTime(r.getCreateTime());
        return vo;
    }
    private PageRequest pageRequest(int page, int size) {
        if (page < 1 || size < 1 || size > MAX_PAGE_SIZE)
            throw new BusinessException(HttpStatus.BAD_REQUEST, "页码必须从1开始，每页数量必须在1到100之间");
        return PageRequest.of(page - 1, size, Sort.by(Sort.Order.desc("createTime"), Sort.Order.desc("id")));
    }
    private String generateRefundNo() { return "REF" + UUID.randomUUID().toString().replace("-", "").substring(0, 24).toUpperCase(); }
    private int defaultZero(Integer value) { return value == null ? 0 : value; }
    private BigDecimal defaultMoney(BigDecimal value) { return value == null ? BigDecimal.ZERO : value; }
    private String trimToNull(String value) { return StringUtils.hasText(value) ? value.trim() : null; }
}


