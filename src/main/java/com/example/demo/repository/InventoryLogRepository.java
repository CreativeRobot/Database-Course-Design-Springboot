package com.example.demo.repository;

import com.example.demo.entity.InventoryChangeType;
import com.example.demo.entity.InventoryLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InventoryLogRepository extends JpaRepository<InventoryLog, Long> {
    Page<InventoryLog> findByBook_IdOrderByCreateTimeDesc(
            Long bookId,
            Pageable pageable
    );

    List<InventoryLog> findByOrder_IdOrderByCreateTimeAsc(Long orderId);

    Page<InventoryLog> findByChangeTypeOrderByCreateTimeDesc(
            InventoryChangeType changeType,
            Pageable pageable
    );
}