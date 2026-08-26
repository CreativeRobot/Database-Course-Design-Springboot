package com.example.demo.controller;

import com.example.demo.entity.BookStatus;
import com.example.demo.service.BookService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AdminBookControllerFilterTests {

    @Mock
    private BookService bookService;

    @InjectMocks
    private AdminBookController controller;

    @Test
    void forwardsRelatedEntityFiltersToTheAdminBookQuery() {
        controller.listBooks(BookStatus.ON_SALE, 7L, 11L, 13L, 2, 20);

        verify(bookService).listAllBooks(BookStatus.ON_SALE, 7L, 11L, 13L, 2, 20);
    }
}
