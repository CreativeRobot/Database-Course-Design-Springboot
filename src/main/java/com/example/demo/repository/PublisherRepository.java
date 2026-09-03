package com.example.demo.repository;

import com.example.demo.entity.Publisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * PublisherRepository 数据访问接口，负责实体持久化及相关查询。
 */
public interface PublisherRepository extends JpaRepository<Publisher, Long> {
    Optional<Publisher> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    Page<Publisher> findByNameContainingIgnoreCase(String name, Pageable pageable);
}