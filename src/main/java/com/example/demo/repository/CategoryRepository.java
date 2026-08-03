package com.example.demo.repository;

import com.example.demo.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    List<Category> findByParentIsNullAndStatusOrderBySortOrderAscNameAsc(Integer status);

    List<Category> findByParent_IdAndStatusOrderBySortOrderAscNameAsc(Long parentId, Integer status);
}