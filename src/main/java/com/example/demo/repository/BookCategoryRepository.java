package com.example.demo.repository;

import com.example.demo.entity.BookCategory;
import com.example.demo.entity.BookCategoryId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookCategoryRepository extends JpaRepository<BookCategory, BookCategoryId> {
    List<BookCategory> findByBook_IdOrderByCategory_NameAsc(Long bookId);

    @Query("""
            select relation
            from BookCategory relation
            join fetch relation.category
            where relation.book.id in :bookIds
            order by relation.category.name asc, relation.book.id asc
            """)
    List<BookCategory> findByBookIdsWithCategory(@Param("bookIds") List<Long> bookIds);

    List<BookCategory> findByCategory_IdOrderByBook_TitleAsc(Long categoryId);

    boolean existsByBook_IdAndCategory_Id(Long bookId, Long categoryId);

    boolean existsByCategory_Id(Long categoryId);

    @Transactional
    long deleteByBook_IdAndCategory_Id(Long bookId, Long categoryId);

    @Transactional
    long deleteByBook_Id(Long bookId);
}
