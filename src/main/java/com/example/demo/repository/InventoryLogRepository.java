package com.example.demo.repository;

import com.example.demo.entity.InventoryChangeType;
import com.example.demo.entity.InventoryLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * InventoryLogRepository 数据访问接口，负责实体持久化及相关查询。
 */
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

    @Query("""
            select log
            from InventoryLog log
            where (:bookId is null or log.book.id = :bookId)
              and (:bookName is null or lower(log.book.title) like lower(concat('%', :bookName, '%')))
              and (:orderId is null or log.order.id = :orderId)
              and (:changeType is null or log.changeType = :changeType)
              and (:startTime is null or log.createTime >= :startTime)
              and (:endTime is null or log.createTime <= :endTime)
            order by log.createTime desc, log.id desc
            """)
    Page<InventoryLog> searchForAdmin(
            @Param("bookId") Long bookId,
            @Param("bookName") String bookName,
            @Param("orderId") Long orderId,
            @Param("changeType") InventoryChangeType changeType,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            Pageable pageable
    );
}
