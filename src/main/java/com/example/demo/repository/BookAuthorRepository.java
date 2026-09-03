package com.example.demo.repository;

import com.example.demo.entity.BookAuthor;
import com.example.demo.entity.BookAuthorId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * BookAuthorRepository 数据访问接口，负责实体持久化及相关查询。
 */
public interface BookAuthorRepository extends JpaRepository<BookAuthor, BookAuthorId> {
    List<BookAuthor> findByBook_IdOrderByAuthorOrderAsc(Long bookId);

    List<BookAuthor> findByAuthor_IdOrderByBook_TitleAsc(Long authorId);

    boolean existsByBook_IdAndAuthor_Id(Long bookId, Long authorId);

    boolean existsByAuthor_Id(Long authorId);

    @Transactional
    long deleteByBook_IdAndAuthor_Id(Long bookId, Long authorId);

    @Transactional
    long deleteByBook_Id(Long bookId);
}
