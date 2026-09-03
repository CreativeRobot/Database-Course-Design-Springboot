package com.example.demo.service;

import com.example.demo.entity.Book;
import com.example.demo.entity.BookPromotion;
import com.example.demo.entity.BookStatus;
import com.example.demo.vo.BookPromotionSummaryVo;
import com.example.demo.vo.BookVo;
import com.example.demo.vo.PromotionHomeVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PromotionHomeService {
    @Autowired private BookPromotionService promotionService;
    @Autowired private BookBundleService bundleService;

    @Transactional(readOnly = true)
    public PromotionHomeVo home(int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 20));
        PromotionHomeVo vo = new PromotionHomeVo();
        vo.setDiscountedBooks(promotionService.listCurrent().stream()
                .filter(promotion -> promotion.getBook().getStatus() == BookStatus.ON_SALE)
                .limit(safeLimit).map(this::toBookVo).toList());
        vo.setBundles(bundleService.listCustomerBundles().stream()
                .limit(safeLimit)
                .map(bundleService::toCustomerVo)
                .toList());
        return vo;
    }

    private BookVo toBookVo(BookPromotion promotion) {
        Book book = promotion.getBook();
        BookVo vo = new BookVo(); vo.setId(book.getId()); vo.setIsbn(book.getIsbn()); vo.setTitle(book.getTitle());
        vo.setPublisherId(book.getPublisher().getId()); vo.setPublisherName(book.getPublisher().getName());
        vo.setOriginalPrice(book.getOriginalPrice()); vo.setBaseSalePrice(book.getSalePrice()); vo.setSalePrice(promotionService.effectivePrice(book));
        vo.setStock(book.getStock()); vo.setStatus(book.getStatus()); vo.setCoverUrl(book.getCoverUrl()); vo.setPromotion(promotionService.toSummary(promotion));
        return vo;
    }
}
