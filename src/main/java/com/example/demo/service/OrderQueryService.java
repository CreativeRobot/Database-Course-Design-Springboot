package com.example.demo.service;

import com.example.demo.common.exception.BusinessException;
import com.example.demo.entity.BookOrder;
import com.example.demo.entity.OrderItem;
import com.example.demo.entity.OrderStatus;
import com.example.demo.entity.User;
import com.example.demo.repository.BookOrderRepository;
import com.example.demo.repository.OrderItemRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.vo.OrderItemVo;
import com.example.demo.vo.OrderVo;
import com.example.demo.vo.PageVo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

/** 订单查询与视图组装服务，避免写事务与查询映射耦合在同一大 Service 中。 */
@Service
public class OrderQueryService {

    private final BookOrderRepository bookOrderRepository;
    private final OrderItemRepository orderItemRepository;
    private final UserRepository userRepository;
public OrderQueryService(BookOrderRepository bookOrderRepository,
                             OrderItemRepository orderItemRepository,
                             UserRepository userRepository) {
        this.bookOrderRepository = bookOrderRepository;
        this.orderItemRepository = orderItemRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public PageVo<OrderVo> listUserOrders(Long userId, OrderStatus status, int page, int size) {
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
        String normalizedOrderNo = StringUtils.hasText(orderNo) ? orderNo.trim() : null;
        Page<BookOrder> orders = bookOrderRepository.searchForAdmin(
                normalizedOrderNo, userId, status, pageable);
        return PageVo.of(orders.map(this::toOrderVoWithItems));
    }

    @Transactional(readOnly = true)
    public OrderVo getAdminOrder(Long orderId) {
        return toOrderVoWithItems(getOrder(orderId));
    }

    private User getActiveUser(Long userId) {
if (userId == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "未登录或Token已失效");
        }
        return userRepository.findByIdAndStatus(userId, 1)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.NOT_FOUND, "用户不存在或已被禁用"));
    }

    private BookOrder getOwnedOrder(Long userId, Long orderId) {
        if (orderId == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "订单不能为空");
        }
        return bookOrderRepository.findByIdAndUser_Id(orderId, userId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "订单不存在"));
    }

    private BookOrder getOrder(Long orderId) {
        if (orderId == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "订单不能为空");
        }
        return bookOrderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "订单不存在"));
    }

    private OrderVo toOrderVoWithItems(BookOrder order) {
        return toOrderVo(order, orderItemRepository.findByOrder_IdOrderByIdAsc(order.getId()));
    }

    private Pageable buildOrderPageable(int page, int size) {
        if (page < 1) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "页码必须从 1 开始");
        }
        if (size < 1 || size > 100) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "每页数量必须在 1 到 100 之间");
        }
        return PageRequest.of(page - 1, size,
                Sort.by(Sort.Order.desc("createTime"), Sort.Order.desc("id")));
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
}
