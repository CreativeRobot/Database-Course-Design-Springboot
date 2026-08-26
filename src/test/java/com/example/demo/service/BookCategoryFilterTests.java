package com.example.demo.service;

import com.example.demo.entity.Category;
import com.example.demo.repository.CategoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookCategoryFilterTests {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private BookService bookService;

    @Test
    void expandsASelectedParentCategoryToItsDirectChildren() {
        Category database = Category.builder().id(2L).name("数据库").build();
        Category softwareEngineering = Category.builder().id(3L).name("软件工程").build();
        when(categoryRepository.findByParent_IdAndStatusOrderBySortOrderAscNameAsc(1L, 1))
                .thenReturn(List.of(database, softwareEngineering));

        List<Long> categoryIds = bookService.resolveSearchCategoryIds(1L);

        assertEquals(List.of(1L, 2L, 3L), categoryIds);
    }
}
