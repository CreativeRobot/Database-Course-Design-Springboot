package com.example.demo.repository;

import com.example.demo.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * CategoryRepository 数据访问接口，负责实体持久化及相关查询。
 */
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByNameIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);

    List<Category> findByParentIsNullAndStatusOrderBySortOrderAscNameAsc(Integer status);

    List<Category> findByParent_IdAndStatusOrderBySortOrderAscNameAsc(Long parentId, Integer status);

    List<Category> findByStatusOrderBySortOrderAscNameAsc(Integer status);

    List<Category> findByNameContainingIgnoreCaseOrderBySortOrderAscNameAsc(String name);

    List<Category> findByStatusAndNameContainingIgnoreCaseOrderBySortOrderAscNameAsc(
            Integer status, String name);

    boolean existsByParent_Id(Long parentId);
}
