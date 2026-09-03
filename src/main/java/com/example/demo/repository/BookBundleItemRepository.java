package com.example.demo.repository;

import com.example.demo.entity.BookBundleItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookBundleItemRepository extends JpaRepository<BookBundleItem, Long> {
    List<BookBundleItem> findByBundle_IdOrderByBook_IdAsc(Long bundleId);
    List<BookBundleItem> findByBook_IdOrderByBundle_IdAsc(Long bookId);
    void deleteByBundle_Id(Long bundleId);
}
