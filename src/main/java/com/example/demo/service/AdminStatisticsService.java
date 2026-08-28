package com.example.demo.service;

import com.example.demo.common.exception.BusinessException;
import com.example.demo.entity.Book;
import com.example.demo.entity.BookStatus;
import com.example.demo.repository.BookOrderRepository;
import com.example.demo.repository.BookRepository;
import com.example.demo.repository.CategorySalesProjection;
import com.example.demo.repository.CompletedSalesSummaryProjection;
import com.example.demo.repository.DailySalesProjection;
import com.example.demo.repository.MonthlySalesProjection;
import com.example.demo.repository.OrderItemRepository;
import com.example.demo.repository.TopBookSalesProjection;
import com.example.demo.vo.AdminStatisticsVo;
import com.example.demo.vo.CategorySalesVo;
import com.example.demo.vo.DailySalesVo;
import com.example.demo.vo.LowStockBookVo;
import com.example.demo.vo.MonthlySalesVo;
import com.example.demo.vo.TopBookSalesVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class AdminStatisticsService {

    private static final int MAX_MONTHS = 24;
    private static final int MAX_TOP = 100;
    private static final int MAX_LOW_STOCK_THRESHOLD = 100_000;
    private static final int DAILY_TREND_DAYS = 7;

    @Autowired
    private BookOrderRepository bookOrderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private BookRepository bookRepository;

    @Transactional(readOnly = true)
    public AdminStatisticsVo getOverview(int months, int top, int lowStockThreshold) {
        validateRange(months, 1, MAX_MONTHS, "统计月份数");
        validateRange(top, 1, MAX_TOP, "热销图书数量");
        validateRange(lowStockThreshold, 0, MAX_LOW_STOCK_THRESHOLD, "库存预警阈值");

        CompletedSalesSummaryProjection summary =
                bookOrderRepository.summarizeCompletedSales();

        AdminStatisticsVo vo = new AdminStatisticsVo();
        vo.setCompletedOrderCount(defaultLong(summary.getCompletedOrderCount()));
        vo.setSalesAmount(defaultAmount(summary.getSalesAmount()));
        vo.setSoldQuantity(sumSoldQuantity());
        vo.setMonthlySales(loadMonthlySales(months));
        vo.setDailySales(loadDailySales());
        vo.setTopBooks(loadTopBooks(top));
        vo.setCategorySales(loadCategorySales());
        vo.setLowStockBooks(loadLowStockBooks(lowStockThreshold));
        return vo;
    }

    private Long sumSoldQuantity() {
        Long quantity = orderItemRepository.sumCompletedSoldQuantity();
        return quantity == null ? 0L : quantity;
    }

    private List<MonthlySalesVo> loadMonthlySales(int months) {
        LocalDateTime startTime = LocalDateTime.now()
                .with(TemporalAdjusters.firstDayOfMonth())
                .minusMonths(months - 1L)
                .withHour(0)
                .withMinute(0)
                .withSecond(0)
                .withNano(0);
        return bookOrderRepository.summarizeMonthlySales(startTime).stream()
                .map(this::toMonthlySalesVo)
                .toList();
    }

    private List<DailySalesVo> loadDailySales() {
        LocalDate startDate = LocalDate.now().minusDays(DAILY_TREND_DAYS - 1L);
        Map<String, DailySalesProjection> salesByDate =
                bookOrderRepository.summarizeDailySales(startDate.atStartOfDay()).stream()
                        .collect(Collectors.toMap(
                                DailySalesProjection::getSaleDate,
                                Function.identity()));
        return IntStream.range(0, DAILY_TREND_DAYS)
                .mapToObj(startDate::plusDays)
                .map(date -> toDailySalesVo(date, salesByDate.get(date.toString())))
                .toList();
    }

    private DailySalesVo toDailySalesVo(
            LocalDate saleDate,
            DailySalesProjection projection
    ) {
        DailySalesVo vo = new DailySalesVo();
        vo.setSaleDate(saleDate.toString());
        vo.setSoldQuantity(projection == null
                ? 0L
                : defaultLong(projection.getSoldQuantity()));
        vo.setSalesAmount(projection == null
                ? BigDecimal.ZERO
                : defaultAmount(projection.getSalesAmount()));
        return vo;
    }
    private List<TopBookSalesVo> loadTopBooks(int top) {
        return bookOrderRepository.findTopSellingBooks(top).stream()
                .map(this::toTopBookSalesVo)
                .toList();
    }

    private List<CategorySalesVo> loadCategorySales() {
        return orderItemRepository.findCategorySales().stream()
                .map(this::toCategorySalesVo)
                .toList();
    }

    private List<LowStockBookVo> loadLowStockBooks(int threshold) {
        return bookRepository.findByStockLessThanEqualAndStatusOrderByStockAsc(
                        threshold, BookStatus.ON_SALE)
                .stream()
                .map(this::toLowStockBookVo)
                .toList();
    }

    private MonthlySalesVo toMonthlySalesVo(MonthlySalesProjection projection) {
        MonthlySalesVo vo = new MonthlySalesVo();
        vo.setSaleMonth(projection.getSaleMonth());
        vo.setCompletedOrderCount(defaultLong(projection.getCompletedOrderCount()));
        vo.setSoldQuantity(defaultLong(projection.getSoldQuantity()));
        vo.setSalesAmount(defaultAmount(projection.getSalesAmount()));
        return vo;
    }

    private TopBookSalesVo toTopBookSalesVo(TopBookSalesProjection projection) {
        TopBookSalesVo vo = new TopBookSalesVo();
        vo.setBookId(projection.getBookId());
        vo.setBookTitle(projection.getBookTitle());
        vo.setSoldQuantity(defaultLong(projection.getSoldQuantity()));
        vo.setSalesAmount(defaultAmount(projection.getSalesAmount()));
        return vo;
    }

    private CategorySalesVo toCategorySalesVo(CategorySalesProjection projection) {
        CategorySalesVo vo = new CategorySalesVo();
        vo.setCategoryId(projection.getCategoryId());
        vo.setCategoryName(projection.getCategoryName());
        vo.setSoldQuantity(defaultLong(projection.getSoldQuantity()));
        vo.setSalesAmount(defaultAmount(projection.getSalesAmount()));
        return vo;
    }

    private LowStockBookVo toLowStockBookVo(Book book) {
        LowStockBookVo vo = new LowStockBookVo();
        vo.setBookId(book.getId());
        vo.setIsbn(book.getIsbn());
        vo.setTitle(book.getTitle());
        vo.setStock(book.getStock());
        vo.setStatus(book.getStatus());
        return vo;
    }

    private void validateRange(int value, int min, int max, String fieldName) {
        if (value < min || value > max) {
            throw new BusinessException(
                    HttpStatus.BAD_REQUEST,
                    fieldName + "必须在" + min + "到" + max + "之间");
        }
    }

    private Long defaultLong(Long value) {
        return value == null ? 0L : value;
    }

    private BigDecimal defaultAmount(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
