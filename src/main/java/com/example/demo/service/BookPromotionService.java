package com.example.demo.service;

import com.example.demo.common.exception.BusinessException;
import com.example.demo.dto.CreateBookPromotionDTO;
import com.example.demo.dto.UpdateBookPromotionDTO;
import com.example.demo.entity.*;
import com.example.demo.repository.BookPromotionRepository;
import com.example.demo.repository.BookRepository;
import com.example.demo.vo.BookPromotionSummaryVo;
import com.example.demo.vo.BookPromotionVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class BookPromotionService {
    @Autowired private BookPromotionRepository promotionRepository;
    @Autowired private BookRepository bookRepository;
    @Autowired private PromotionPricingService pricingService;

    @Transactional(readOnly = true)
    public List<BookPromotionVo> listAdmin() {
        return promotionRepository.findAllWithBook().stream().map(this::toAdminVo).toList();
    }

    @Transactional
    public BookPromotionVo create(CreateBookPromotionDTO dto) {
        validate(dto == null ? null : dto.getName(), dto == null ? null : dto.getDescription(),
                dto == null ? null : dto.getDiscountPercent(), dto == null ? null : dto.getStartTime(), dto == null ? null : dto.getEndTime());
        Book book = bookRepository.findById(dto.getBookId())
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "图书不存在"));
        ensureNoOverlap(book.getId(), dto.getStartTime(), dto.getEndTime(), null);
        BookPromotion promotion = BookPromotion.builder().book(book).name(dto.getName().trim())
                .description(trimToNull(dto.getDescription())).discountPercent(dto.getDiscountPercent())
                .startTime(dto.getStartTime()).endTime(dto.getEndTime()).status(BookPromotionStatus.ACTIVE).build();
        return toAdminVo(promotionRepository.save(promotion));
    }

    @Transactional
    public BookPromotionVo update(Long id, UpdateBookPromotionDTO dto) {
        validate(dto == null ? null : dto.getName(), dto == null ? null : dto.getDescription(),
                dto == null ? null : dto.getDiscountPercent(), dto == null ? null : dto.getStartTime(), dto == null ? null : dto.getEndTime());
        BookPromotion promotion = getForUpdate(id);
        if (dto.getVersion() == null || !dto.getVersion().equals(promotion.getVersion())) {
            throw new BusinessException(HttpStatus.CONFLICT, "活动已被其他管理员修改，请刷新后重试");
        }
        ensureNoOverlap(promotion.getBook().getId(), dto.getStartTime(), dto.getEndTime(), id);
        promotion.setName(dto.getName().trim()); promotion.setDescription(trimToNull(dto.getDescription()));
        promotion.setDiscountPercent(dto.getDiscountPercent()); promotion.setStartTime(dto.getStartTime()); promotion.setEndTime(dto.getEndTime());
        return toAdminVo(promotionRepository.save(promotion));
    }

    @Transactional
    public BookPromotionVo changeStatus(Long id, BookPromotionStatus status) {
        if (status == null) throw new BusinessException(HttpStatus.BAD_REQUEST, "状态不能为空");
        BookPromotion promotion = getForUpdate(id);
        promotion.setStatus(status);
        return toAdminVo(promotionRepository.save(promotion));
    }

    @Transactional(readOnly = true)
    public List<BookPromotion> listCurrent() {
        return promotionRepository.findActiveWithBook(BookPromotionStatus.ACTIVE, LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public Optional<BookPromotion> current(Long bookId) {
        if (bookId == null) return Optional.empty();
        return promotionRepository.findCurrentByBookId(bookId, BookPromotionStatus.ACTIVE, LocalDateTime.now()).stream().findFirst();
    }

    @Transactional(readOnly = true)
    public BigDecimal effectivePrice(Book book) {
        if (book == null) return null;
        return effectivePrice(book.getId(), book.getSalePrice());
    }

    @Transactional(readOnly = true)
    public BigDecimal effectivePrice(Long bookId, BigDecimal baseSalePrice) {
        return current(bookId).map(promotion -> pricingService.effectivePrice(baseSalePrice, promotion.getStatus(),
                promotion.getDiscountPercent(), promotion.getStartTime(), promotion.getEndTime(), LocalDateTime.now()))
                .orElseGet(() -> pricingService.money(baseSalePrice));
    }

    public BookPromotionSummaryVo toSummary(BookPromotion promotion) {
        if (promotion == null) return null;
        BookPromotionSummaryVo vo = new BookPromotionSummaryVo();
        vo.setId(promotion.getId()); vo.setName(promotion.getName()); vo.setDescription(promotion.getDescription());
        vo.setDiscountPercent(promotion.getDiscountPercent()); vo.setStartTime(promotion.getStartTime()); vo.setEndTime(promotion.getEndTime());
        return vo;
    }

    private BookPromotionVo toAdminVo(BookPromotion promotion) {
        BookPromotionVo vo = new BookPromotionVo();
        BookPromotionSummaryVo summary = toSummary(promotion);
        vo.setId(summary.getId()); vo.setName(summary.getName()); vo.setDescription(summary.getDescription());
        vo.setDiscountPercent(summary.getDiscountPercent()); vo.setStartTime(summary.getStartTime()); vo.setEndTime(summary.getEndTime());
        Book book = promotion.getBook(); vo.setBookId(book.getId()); vo.setBookTitle(book.getTitle()); vo.setCoverUrl(book.getCoverUrl());
        vo.setBaseSalePrice(pricingService.money(book.getSalePrice())); vo.setPromotionPrice(effectivePrice(book));
        vo.setStatus(promotion.getStatus()); vo.setVersion(promotion.getVersion()); vo.setCreateTime(promotion.getCreateTime()); vo.setUpdateTime(promotion.getUpdateTime());
        return vo;
    }

    private BookPromotion getForUpdate(Long id) {
        return promotionRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "活动不存在"));
    }

    private void validate(String name, String description, Integer discountPercent, LocalDateTime startTime, LocalDateTime endTime) {
        if (!StringUtils.hasText(name) || name.trim().length() > 100) throw new BusinessException(HttpStatus.BAD_REQUEST, "活动名称长度必须在1到100之间");
        if (description != null && description.length() > 500) throw new BusinessException(HttpStatus.BAD_REQUEST, "活动说明不能超过500个字符");
        if (discountPercent == null || discountPercent < 1 || discountPercent > 99) throw new BusinessException(HttpStatus.BAD_REQUEST, "折扣必须为1到99之间的整数");
        if (startTime == null || endTime == null || !endTime.isAfter(startTime)) throw new BusinessException(HttpStatus.BAD_REQUEST, "结束时间必须晚于开始时间");
    }

    private void ensureNoOverlap(Long bookId, LocalDateTime startTime, LocalDateTime endTime, Long excludedId) {
        if (promotionRepository.existsOverlapping(bookId, startTime, endTime, excludedId)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "同一本图书的活动时间不能重叠");
        }
    }

    private String trimToNull(String value) { return StringUtils.hasText(value) ? value.trim() : null; }
}
