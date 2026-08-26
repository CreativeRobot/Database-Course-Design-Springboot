package com.example.demo.service;

import com.example.demo.common.exception.BusinessException;
import com.example.demo.dto.SaveCategoryDTO;
import com.example.demo.entity.Category;
import com.example.demo.repository.BookCategoryRepository;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.vo.CategoryVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** 分类管理业务，负责分类层级、状态和删除约束校验。 */
@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private BookCategoryRepository bookCategoryRepository;

    // ==================== 管理端查询 ====================

    /** 查询全部分类，可按启用状态过滤。 */
    @Transactional(readOnly = true)
    public List<CategoryVo> listCategories(Integer status) {
        if (status != null) {
            validateStatus(status);
        }
        List<Category> categories = status == null
                ? categoryRepository.findAll(Sort.by(
                        Sort.Order.asc("sortOrder"), Sort.Order.asc("name")))
                : categoryRepository.findByStatusOrderBySortOrderAscNameAsc(status);
        return categories.stream().map(this::toVo).toList();
    }


    /** 按父子层级查询分类树，可按启用状态过滤。 */
    @Transactional(readOnly = true)
    public List<CategoryVo> listCategoryTree(Integer status) {
        List<CategoryVo> categories = listCategories(status);
        java.util.Map<Long, CategoryVo> categoriesById = new java.util.LinkedHashMap<>();
        for (CategoryVo category : categories) {
            categoriesById.put(category.getId(), category);
        }

        List<CategoryVo> roots = new java.util.ArrayList<>();
        for (CategoryVo category : categories) {
            if (category.getParentId() == null) {
                roots.add(category);
                continue;
            }
            CategoryVo parent = categoriesById.get(category.getParentId());
            if (parent != null) {
                parent.getChildren().add(category);
            }
        }
        return roots;
    }
    /** 查询分类详情。 */
    @Transactional(readOnly = true)
    public CategoryVo getCategory(Long categoryId) {
        return toVo(getCategoryOrThrow(categoryId));
    }

    // ==================== 管理端写操作 ====================

    /** 新增分类，可指定父分类、排序值和初始状态。 */
    @Transactional
    public CategoryVo createCategory(SaveCategoryDTO dto) {
        validateRequest(dto);
        String name = dto.getName().trim();
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new BusinessException(HttpStatus.CONFLICT, "分类名称已存在");
        }

        Category category = Category.builder()
                .name(name)
                .parent(resolveParent(dto.getParentId(), null))
                .sortOrder(dto.getSortOrder())
                .status(dto.getStatus())
                .build();
        return toVo(categoryRepository.save(category));
    }

    /** 完整更新分类信息，并防止分类成为自身或后代分类的子节点。 */
    @Transactional
    public CategoryVo updateCategory(Long categoryId, SaveCategoryDTO dto) {
        validateRequest(dto);
        Category category = getCategoryOrThrow(categoryId);
        String name = dto.getName().trim();
        categoryRepository.findByNameIgnoreCase(name)
                .filter(existing -> !existing.getId().equals(categoryId))
                .ifPresent(existing -> {
                    throw new BusinessException(HttpStatus.CONFLICT, "分类名称已存在");
                });

        if (dto.getParentId() != null && categoryRepository.existsByParent_Id(categoryId)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST,
                    "含有子分类的一级分类不能移动到其他分类下");
        }

        category.setName(name);
        category.setParent(resolveParent(dto.getParentId(), categoryId));
        category.setSortOrder(dto.getSortOrder());
        category.setStatus(dto.getStatus());
        return toVo(categoryRepository.save(category));
    }

    /** 启用或停用分类。 */
    @Transactional
    public CategoryVo changeStatus(Long categoryId, Integer status) {
        validateStatus(status);
        Category category = getCategoryOrThrow(categoryId);
        category.setStatus(status);
        return toVo(categoryRepository.save(category));
    }

    /**
     * 删除未被使用的分类。
     * 存在子分类或图书关联时拒绝删除，避免破坏分类树和图书数据。
     */
    @Transactional
    public void deleteCategory(Long categoryId) {
        Category category = getCategoryOrThrow(categoryId);
        if (categoryRepository.existsByParent_Id(categoryId)) {
            throw new BusinessException(HttpStatus.CONFLICT, "该分类存在子分类，无法删除");
        }
        if (bookCategoryRepository.existsByCategory_Id(categoryId)) {
            throw new BusinessException(HttpStatus.CONFLICT, "该分类已关联图书，无法删除");
        }
        categoryRepository.delete(category);
    }

    // ==================== 私有辅助方法 ====================

    private Category resolveParent(Long parentId, Long categoryId) {
        if (parentId == null) {
            return null;
        }
        if (parentId.equals(categoryId)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "分类不能作为自己的父分类");
        }

        Category parent = categoryRepository.findById(parentId)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.BAD_REQUEST, "父分类不存在"));
        if (parent.getParent() != null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "分类最多支持两级，父分类必须为一级分类");
        }

        Set<Long> visited = new HashSet<>();
        Category cursor = parent;
        while (cursor != null) {
            if (!visited.add(cursor.getId())) {
                throw new BusinessException(HttpStatus.CONFLICT, "分类层级数据存在循环");
            }
            if (cursor.getId().equals(categoryId)) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "不能将分类移动到其子分类下");
            }
            cursor = cursor.getParent();
        }
        return parent;
    }

    private Category getCategoryOrThrow(Long categoryId) {
        if (categoryId == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "分类不能为空");
        }
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "分类不存在"));
    }

    private void validateRequest(SaveCategoryDTO dto) {
        if (dto == null || !StringUtils.hasText(dto.getName())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "分类名称不能为空");
        }
        if (dto.getSortOrder() == null || dto.getSortOrder() < 0) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "排序值不能为负数");
        }
        validateStatus(dto.getStatus());
    }

    private void validateStatus(Integer status) {
        if (status == null || (status != 0 && status != 1)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "分类状态只能为0或1");
        }
    }

    private CategoryVo toVo(Category category) {
        CategoryVo vo = new CategoryVo();
        vo.setId(category.getId());
        vo.setName(category.getName());
        if (category.getParent() != null) {
            vo.setParentId(category.getParent().getId());
            vo.setParentName(category.getParent().getName());
        }
        vo.setSortOrder(category.getSortOrder());
        vo.setStatus(category.getStatus());
        vo.setCreateTime(category.getCreateTime());
        vo.setUpdateTime(category.getUpdateTime());
        return vo;
    }
}
