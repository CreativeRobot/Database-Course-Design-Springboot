package com.example.demo.repository;

import com.example.demo.entity.Book;
import com.example.demo.entity.BookStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {
    Optional<Book> findByIsbn(String isbn);

    boolean existsByIsbn(String isbn);

    boolean existsByPublisher_Id(Long publisherId);

    Page<Book> findByStatus(BookStatus status, Pageable pageable);

    Page<Book> findByTitleContainingIgnoreCaseAndStatus(
            String title,
            BookStatus status,
            Pageable pageable
    );

    Page<Book> findByPublisher_IdAndStatus(
            Long publisherId,
            BookStatus status,
            Pageable pageable
    );

    List<Book> findByStockLessThanEqualAndStatusOrderByStockAsc(
            Integer stock,
            BookStatus status
    );

    @Query("""
            select bookAuthor.book
            from BookAuthor bookAuthor
            where bookAuthor.author.id = :authorId
              and bookAuthor.book.status = :status
            """)
    Page<Book> findByAuthorIdAndStatus(
            @Param("authorId") Long authorId,
            @Param("status") BookStatus status,
            Pageable pageable
    );

    @Query("""
            select bookCategory.book
            from BookCategory bookCategory
            where bookCategory.category.id = :categoryId
              and bookCategory.book.status = :status
            """)
    Page<Book> findByCategoryIdAndStatus(
            @Param("categoryId") Long categoryId,
            @Param("status") BookStatus status,
            Pageable pageable
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("""
            update Book book
            set book.stock = book.stock - :quantity
            where book.id = :bookId
              and book.status = :status
              and book.stock >= :quantity
              and :quantity > 0
            """)
    int decreaseStock(
            @Param("bookId") Long bookId,
            @Param("quantity") Integer quantity,
            @Param("status") BookStatus status
    );

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("""
            update Book book
            set book.stock = book.stock + :quantity
            where book.id = :bookId
              and :quantity > 0
            """)
    int increaseStock(
            @Param("bookId") Long bookId,
            @Param("quantity") Integer quantity
    );
}
