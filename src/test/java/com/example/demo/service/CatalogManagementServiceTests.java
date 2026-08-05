package com.example.demo.service;

import com.example.demo.common.exception.BusinessException;
import com.example.demo.dto.SaveCategoryDTO;
import com.example.demo.dto.SavePublisherDTO;
import com.example.demo.entity.Author;
import com.example.demo.entity.Category;
import com.example.demo.entity.Publisher;
import com.example.demo.repository.AuthorRepository;
import com.example.demo.repository.BookAuthorRepository;
import com.example.demo.repository.BookCategoryRepository;
import com.example.demo.repository.BookRepository;
import com.example.demo.repository.CategoryRepository;
import com.example.demo.repository.PublisherRepository;
import com.example.demo.vo.CategoryVo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CatalogManagementServiceTests {

    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private BookCategoryRepository bookCategoryRepository;
    @Mock
    private AuthorRepository authorRepository;
    @Mock
    private BookAuthorRepository bookAuthorRepository;
    @Mock
    private PublisherRepository publisherRepository;
    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private CategoryService categoryService;
    @InjectMocks
    private AuthorService authorService;
    @InjectMocks
    private PublisherService publisherService;

    @Test
    void createCategoryWithValidatedDefaults() {
        SaveCategoryDTO dto = categoryDto("数据库", null);
        when(categoryRepository.existsByNameIgnoreCase("数据库")).thenReturn(false);
        when(categoryRepository.save(any(Category.class))).thenAnswer(invocation -> {
            Category category = invocation.getArgument(0);
            category.setId(1L);
            return category;
        });

        CategoryVo result = categoryService.createCategory(dto);

        assertEquals("数据库", result.getName());
        assertEquals(0, result.getSortOrder());
        assertEquals(1, result.getStatus());
    }

    @Test
    void updateCategoryRejectsMovingUnderItsChild() {
        Category root = Category.builder().id(1L).name("计算机").build();
        Category child = Category.builder().id(2L).name("数据库").parent(root).build();
        SaveCategoryDTO dto = categoryDto("计算机", 2L);

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(root));
        when(categoryRepository.findByNameIgnoreCase("计算机")).thenReturn(Optional.of(root));
        when(categoryRepository.findById(2L)).thenReturn(Optional.of(child));

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> categoryService.updateCategory(1L, dto));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void deleteAuthorRejectsLinkedBooks() {
        Author author = Author.builder().id(3L).name("王珊").build();
        when(authorRepository.findById(3L)).thenReturn(Optional.of(author));
        when(bookAuthorRepository.existsByAuthor_Id(3L)).thenReturn(true);

        BusinessException exception = assertThrows(
                BusinessException.class, () -> authorService.deleteAuthor(3L));

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        verify(authorRepository, never()).delete(any());
    }

    @Test
    void createPublisherRejectsDuplicateName() {
        SavePublisherDTO dto = new SavePublisherDTO();
        dto.setName("高等教育出版社");
        when(publisherRepository.existsByNameIgnoreCase("高等教育出版社"))
                .thenReturn(true);

        BusinessException exception = assertThrows(
                BusinessException.class, () -> publisherService.createPublisher(dto));

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        verify(publisherRepository, never()).save(any(Publisher.class));
    }

    @Test
    void deletePublisherRejectsLinkedBooks() {
        Publisher publisher = Publisher.builder()
                .id(4L)
                .name("高等教育出版社")
                .build();
        when(publisherRepository.findById(4L)).thenReturn(Optional.of(publisher));
        when(bookRepository.existsByPublisher_Id(4L)).thenReturn(true);

        BusinessException exception = assertThrows(
                BusinessException.class, () -> publisherService.deletePublisher(4L));

        assertEquals(HttpStatus.CONFLICT, exception.getStatus());
        verify(publisherRepository, never()).delete(any());
    }

    private SaveCategoryDTO categoryDto(String name, Long parentId) {
        SaveCategoryDTO dto = new SaveCategoryDTO();
        dto.setName(name);
        dto.setParentId(parentId);
        return dto;
    }
}
