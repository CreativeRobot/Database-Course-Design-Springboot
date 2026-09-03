package com.example.demo.repository;

import com.example.demo.entity.BookBundle;
import com.example.demo.entity.BookBundleStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookBundleRepository extends JpaRepository<BookBundle, Long> {
    List<BookBundle> findAllByOrderByIdAsc();
    List<BookBundle> findByStatusOrderByIdAsc(BookBundleStatus status);

    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("select b from BookBundle b where b.id = :id")
    Optional<BookBundle> findByIdForShare(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from BookBundle b where b.id = :id")
    Optional<BookBundle> findByIdForUpdate(@Param("id") Long id);
}
