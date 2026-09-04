package com.example.demo.service;

import com.example.demo.entity.*;
import com.example.demo.repository.BundleRefundRequestRepository;
import com.example.demo.repository.OrderBundleApplicationItemRepository;
import com.example.demo.repository.RefundRequestRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class RefundAvailabilityService {
    @Autowired private RefundRequestRepository refundRequestRepository;
    @Autowired private OrderBundleApplicationItemRepository bundleItemRepository;
    @Autowired private BundleRefundRequestRepository bundleRefundRequestRepository;

    public RefundAvailability forItem(OrderItem item) {
        List<OrderBundleApplicationItem> bundleItems = bundleItemRepository
                .findByOrderItem_IdOrderByIdAsc(item.getId());
        int covered = bundleItems.stream().mapToInt(i -> i.getQuantity() == null ? 0 : i.getQuantity()).sum();
        int approved = zero(refundRequestRepository.sumQuantityByOrderItemIdAndStatus(item.getId(), RefundStatus.APPROVED));
        int pending = zero(refundRequestRepository.sumQuantityByOrderItemIdAndStatus(item.getId(), RefundStatus.PENDING));
        int standalone = Math.max(0, zero(item.getQuantity()) - covered);
        int refundable = Math.max(0, standalone - approved - pending);
        return new RefundAvailability(covered, standalone, approved, pending, refundable);
    }

    public BundleEligibility forBundle(OrderBundleApplication application) {
        List<BundleRefundRequest> requests = bundleRefundRequestRepository.findByApplicationIdAndStatusIn(
                application.getId(), List.of(RefundStatus.PENDING, RefundStatus.APPROVED));
        if (!requests.isEmpty()) {
            RefundStatus status = requests.get(0).getStatus();
            return new BundleEligibility(false, status, status == RefundStatus.PENDING
                    ? "整包退款审核中" : "整包退款已通过", application.getBundlePrice());
        }
        List<OrderBundleApplicationItem> snapshots = bundleItemRepository
                .findByApplication_IdOrderByIdAsc(application.getId());
        for (OrderBundleApplicationItem snapshot : snapshots) {
            OrderItem item = snapshot.getOrderItem();
            if (refundRequestRepository.existsLegacyActiveForOrderItem(item.getId())) {
                return new BundleEligibility(false, null, "组合成员存在历史普通售后", application.getBundlePrice());
            }
            if (zero(item.getRefundedQuantity()) + zero(snapshot.getQuantity()) > zero(item.getQuantity())) {
                return new BundleEligibility(false, null, "组合成员可退款数量已被占用", application.getBundlePrice());
            }
        }
        return new BundleEligibility(true, null, null, application.getBundlePrice());
    }

    public int bundleCoveredQuantity(OrderItem item) {
        return forItem(item).bundleCoveredQuantity();
    }

    private int zero(Integer value) { return value == null ? 0 : value; }

    public record BundleEligibility(boolean refundable, RefundStatus status, String unavailableReason,
                                    BigDecimal amount) { }
}
