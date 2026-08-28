package com.example.demo.service;

import com.example.demo.common.exception.BusinessException;
import com.example.demo.entity.Book;
import com.example.demo.entity.BookStatus;
import com.example.demo.repository.BookOrderRepository;
import com.example.demo.repository.BookRepository;
import com.example.demo.repository.CategorySalesProjection;
import com.example.demo.repository.CompletedSalesSummaryProjection;
import com.example.demo.repository.MonthlySalesProjection;
import com.example.demo.repository.DailySalesProjection;
import com.example.demo.repository.OrderItemRepository;
import com.example.demo.repository.TopBookSalesProjection;
import com.example.demo.vo.AdminStatisticsVo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminStatisticsServiceTests {

    @Mock
    private BookOrderRepository bookOrderRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private CompletedSalesSummaryProjection summary;
    @Mock
    private MonthlySalesProjection monthlySales;
    @Mock
    private DailySalesProjection dailySales;
    @Mock
    private TopBookSalesProjection topBookSales;
    @Mock
    private CategorySalesProjection categorySales;

    @InjectMocks
    private AdminStatisticsService adminStatisticsService;

    @Test
    void overviewAggregatesCompletedSalesAndLowStockBooks() {
        when(summary.getCompletedOrderCount()).thenReturn(3L);
        when(summary.getSalesAmount()).thenReturn(new BigDecimal("99.90"));
        when(bookOrderRepository.summarizeCompletedSales()).thenReturn(summary);
        when(orderItemRepository.sumCompletedSoldQuantity()).thenReturn(7L);
        when(bookOrderRepository.summarizeMonthlySales(any())).thenReturn(List.of(monthlySales));
        when(bookOrderRepository.summarizeDailySales(any())).thenReturn(List.of(dailySales));
        when(bookOrderRepository.findTopSellingBooks(10)).thenReturn(List.of(topBookSales));
        when(orderItemRepository.findCategorySales()).thenReturn(List.of(categorySales));
        when(bookRepository.findByStockLessThanEqualAndStatusOrderByStockAsc(
                5, BookStatus.ON_SALE)).thenReturn(List.of(
                Book.builder().id(1L).isbn("9780000000001").title("数据库系统").stock(2)
                        .status(BookStatus.ON_SALE).build()));

        when(monthlySales.getSaleMonth()).thenReturn("2026-08");
        when(monthlySales.getCompletedOrderCount()).thenReturn(2L);
        when(monthlySales.getSoldQuantity()).thenReturn(4L);
        when(monthlySales.getSalesAmount()).thenReturn(new BigDecimal("60.00"));
        when(dailySales.getSaleDate()).thenReturn(LocalDate.now().toString());
        when(dailySales.getSoldQuantity()).thenReturn(4L);
        when(dailySales.getSalesAmount()).thenReturn(new BigDecimal("60.00"));
        when(topBookSales.getBookId()).thenReturn(1L);
        when(topBookSales.getBookTitle()).thenReturn("数据库系统");
        when(topBookSales.getSoldQuantity()).thenReturn(4L);
        when(topBookSales.getSalesAmount()).thenReturn(new BigDecimal("60.00"));
        when(categorySales.getCategoryId()).thenReturn(1L);
        when(categorySales.getCategoryName()).thenReturn("计算机");
        when(categorySales.getSoldQuantity()).thenReturn(4L);
        when(categorySales.getSalesAmount()).thenReturn(new BigDecimal("60.00"));

        AdminStatisticsVo result = adminStatisticsService.getOverview(6, 10, 5);

        assertEquals(3L, result.getCompletedOrderCount());
        assertEquals(new BigDecimal("99.90"), result.getSalesAmount());
        assertEquals(7L, result.getSoldQuantity());
        assertEquals(1, result.getMonthlySales().size());
        assertEquals(7, result.getDailySales().size());
        assertEquals(0L, result.getDailySales().getFirst().getSoldQuantity());
        assertEquals(BigDecimal.ZERO, result.getDailySales().getFirst().getSalesAmount());
        assertEquals(LocalDate.now().toString(), result.getDailySales().getLast().getSaleDate());
        assertEquals(4L, result.getDailySales().getLast().getSoldQuantity());
        assertEquals(new BigDecimal("60.00"), result.getDailySales().getLast().getSalesAmount());
        assertEquals(1, result.getTopBooks().size());
        assertEquals(1, result.getCategorySales().size());
        assertEquals(2, result.getLowStockBooks().getFirst().getStock());
    }

    @Test
    void overviewRejectsInvalidParameters() {
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> adminStatisticsService.getOverview(0, 10, 5));

        assertEquals(400, exception.getStatus().value());
    }
}
