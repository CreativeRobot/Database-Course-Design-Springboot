package com.example.demo.repository;

import com.example.demo.entity.BookCategory;
import com.example.demo.entity.BookCategoryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface BookCategoryRepository extends JpaRepository<BookCategory, BookCategoryId> {
    List<BookCategory> findByBook_IdOrderByCategory_NameAsc(Long bookId);

    List<BookCategory> findByCategory_IdOrderByBook_TitleAsc(Long categoryId);

    boolean existsByBook_IdAndCategory_Id(Long bookId, Long categoryId);

    boolean existsByCategory_Id(Long categoryId);

    @Transactional
    long deleteByBook_IdAndCategory_Id(Long bookId, Long categoryId);

    @Transactional
    long deleteByBook_Id(Long bookId);
}
