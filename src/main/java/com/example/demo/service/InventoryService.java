package com.example.demo.service;

import com.example.demo.common.exception.BusinessException;
import com.example.demo.entity.Book;
import com.example.demo.entity.BookOrder;
import com.example.demo.entity.InventoryChangeType;
import com.example.demo.entity.InventoryLog;
import com.example.demo.entity.OrderItem;
import com.example.demo.repository.BookOrderRepository;
import com.example.demo.repository.BookRepository;
import com.example.demo.repository.InventoryLogRepository;
import com.example.demo.repository.OrderItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/** Inventory boundary for stock returns and completed sales updates. */
@Service
public class InventoryService {

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private BookOrderRepository bookOrderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private InventoryLogRepository inventoryLogRepository;

    @Transactional
    public void returnStockAndWriteLogs(Long orderId, String orderNo) {
        List<OrderStockLine> lines = orderItemRepository
                .findByOrder_IdOrderByIdAsc(orderId)
                .stream()
                .map(item -> new OrderStockLine(item.getBook().getId(), item.getQuantity()))
                .sorted(Comparator.comparing(OrderStockLine::bookId))
                .toList();
        if (lines.isEmpty()) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "订单明细不存在，无法退回库存");
        }

        List<ReturnedStock> returnedStocks = new ArrayList<>();
        for (OrderStockLine line : lines) {
            if (bookRepository.increaseStock(line.bookId(), line.quantity()) == 0) {
                throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "订单库存退回失败");
            }
            Book book = bookRepository.findById(line.bookId())
                    .orElseThrow(() -> new BusinessException(
                            HttpStatus.INTERNAL_SERVER_ERROR, "订单关联图书不存在"));
            returnedStocks.add(new ReturnedStock(
                    line.bookId(), line.quantity(), book.getStock() - line.quantity(), book.getStock()));
        }

        BookOrder orderReference = bookOrderRepository.getReferenceById(orderId);
        List<InventoryLog> logs = returnedStocks.stream()
                .map(stock -> InventoryLog.builder()
                        .book(bookRepository.getReferenceById(stock.bookId()))
                        .changeQuantity(stock.quantity())
                        .beforeStock(stock.beforeStock())
                        .afterStock(stock.afterStock())
                        .changeType(InventoryChangeType.ORDER_CANCEL_RETURN)
                        .order(orderReference)
                        .remark("订单" + orderNo + "取消退回库存")
                        .build())
                .toList();
        inventoryLogRepository.saveAllAndFlush(logs);
    }

    @Transactional
    public void increaseSalesCount(List<OrderItem> items) {
        for (OrderItem item : items) {
            bookRepository.increaseSalesCount(item.getBook().getId(), item.getQuantity());
        }
    }

    public record StockReservation(
            Long bookId, Integer quantity, Integer beforeStock, Integer afterStock) {
    }

    private record OrderStockLine(Long bookId, Integer quantity) {
    }

    private record ReturnedStock(
            Long bookId, Integer quantity, Integer beforeStock, Integer afterStock) {
    }
}
