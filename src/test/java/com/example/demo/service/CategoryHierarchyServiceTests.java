package com.example.demo.service;

import com.example.demo.common.exception.BusinessException;
import com.example.demo.dto.SaveCategoryDTO;
import com.example.demo.entity.Category;
import com.example.demo.repository.BookCategoryRepository;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.vo.CategoryVo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryHierarchyServiceTests {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private BookCategoryRepository bookCategoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void createsTreeWithChildrenNestedUnderTheirRoot() {
        Category computer = category(1L, "计算机", null);
        Category database = category(2L, "数据库", computer);
        Category literature = category(3L, "文学", null);
        when(categoryRepository.findByStatusOrderBySortOrderAscNameAsc(1))
                .thenReturn(List.of(computer, database, literature));

        List<CategoryVo> tree = categoryService.listCategoryTree(1);

        assertEquals(List.of("计算机", "文学"), tree.stream().map(CategoryVo::getName).toList());
        assertEquals(List.of("数据库"), tree.getFirst().getChildren().stream()
                .map(CategoryVo::getName).toList());
    }

    @Test
    void rejectsUsingASecondLevelCategoryAsParent() {
        Category computer = category(1L, "计算机", null);
        Category database = category(2L, "数据库", computer);
        SaveCategoryDTO dto = categoryDto("MySQL", 2L);
        when(categoryRepository.existsByNameIgnoreCase("MySQL")).thenReturn(false);
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(database));

        BusinessException exception = assertThrows(
                BusinessException.class, () -> categoryService.createCategory(dto));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void rejectsMovingAParentThatStillHasChildrenBelowAnotherRoot() {
        Category computer = category(1L, "计算机", null);
        SaveCategoryDTO dto = categoryDto("计算机", 2L);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(computer));
        when(categoryRepository.findByNameIgnoreCase("计算机")).thenReturn(Optional.of(computer));
        when(categoryRepository.existsByParent_Id(1L)).thenReturn(true);

        BusinessException exception = assertThrows(
                BusinessException.class, () -> categoryService.updateCategory(1L, dto));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verify(categoryRepository, never()).save(any());
    }

    private SaveCategoryDTO categoryDto(String name, Long parentId) {
        SaveCategoryDTO dto = new SaveCategoryDTO();
        dto.setName(name);
        dto.setParentId(parentId);
        dto.setSortOrder(0);
        dto.setStatus(1);
        return dto;
    }

    private Category category(Long id, String name, Category parent) {
        return Category.builder()
                .id(id)
                .name(name)
                .parent(parent)
                .sortOrder(0)
                .status(1)
                .build();
    }
}
