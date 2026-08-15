package com.example.demo.service;

import com.example.demo.common.exception.BusinessException;
import com.example.demo.entity.InventoryChangeType;
import com.example.demo.entity.InventoryLog;
import com.example.demo.repository.InventoryLogRepository;
import com.example.demo.vo.InventoryLogVo;
import com.example.demo.vo.PageVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class InventoryLogService {

    private static final int MAX_PAGE_SIZE = 100;

    @Autowired
    private InventoryLogRepository inventoryLogRepository;

    @Transactional(readOnly = true)
    public PageVo<InventoryLogVo> listLogs(
            Long bookId,
            Long orderId,
            InventoryChangeType changeType,
            LocalDateTime startTime,
            LocalDateTime endTime,
            int page,
            int size) {
        validatePositive(bookId, "图书 ID");
        validatePositive(orderId, "订单 ID");
        if (startTime != null && endTime != null && !startTime.isBefore(endTime)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "开始时间必须早于结束时间");
        }
        if (page < 1 || size < 1 || size > MAX_PAGE_SIZE) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    "页码必须从1开始，每页数量必须在1到" + MAX_PAGE_SIZE + "之间"
            );
        }

        PageRequest pageable = PageRequest.of(
                page - 1,
                size,
                Sort.by(
                        Sort.Order.desc("createTime"),
                        Sort.Order.desc("id")
                )
        );
        return PageVo.of(inventoryLogRepository.searchForAdmin(
                bookId, orderId, changeType, startTime, endTime, pageable
        ).map(this::toVo));
    }

    private InventoryLogVo toVo(InventoryLog log) {
        InventoryLogVo vo = new InventoryLogVo();
        vo.setId(log.getId());
        vo.setBookId(log.getBook().getId());
        vo.setIsbn(log.getBook().getIsbn());
        vo.setBookTitle(log.getBook().getTitle());
        vo.setChangeQuantity(log.getChangeQuantity());
        vo.setBeforeStock(log.getBeforeStock());
        vo.setAfterStock(log.getAfterStock());
        vo.setChangeType(log.getChangeType());
        if (log.getOrder() != null) {
            vo.setOrderId(log.getOrder().getId());
            vo.setOrderNo(log.getOrder().getOrderNo());
        }
        vo.setRemark(log.getRemark());
        vo.setCreateTime(log.getCreateTime());
        return vo;
    }

    private void validatePositive(Long value, String fieldName) {
        if (value != null && value <= 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, fieldName + "必须为正数");
        }
    }
}
