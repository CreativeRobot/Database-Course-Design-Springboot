package com.example.demo.repository;

import com.example.demo.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUser_IdOrderByCreateTimeDesc(Long userId);

    List<CartItem> findByUser_IdAndSelectedTrueOrderByCreateTimeDesc(Long userId);

    Optional<CartItem> findByUser_IdAndBook_Id(Long userId, Long bookId);

    @Transactional
    long deleteByUser_IdAndBook_Id(Long userId, Long bookId);

    @Transactional
    long deleteByUser_IdAndSelectedTrue(Long userId);
}