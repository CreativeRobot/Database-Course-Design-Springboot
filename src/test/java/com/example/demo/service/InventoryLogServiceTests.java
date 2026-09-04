package com.example.demo.service;

import com.example.demo.repository.InventoryLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryLogServiceTests {

    @Mock
    private InventoryLogRepository inventoryLogRepository;

    @InjectMocks
    private InventoryLogService inventoryLogService;

    @Test
    void trimsBookNameBeforeSearchingInventoryLogs() {
        when(inventoryLogRepository.searchForAdmin(
                isNull(), eq("数据库"), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)
        )).thenReturn(Page.empty());

        inventoryLogService.listLogs(
                null, "  数据库  ", null, null, null, null, 1, 20
        );

        verify(inventoryLogRepository).searchForAdmin(
                isNull(), eq("数据库"), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)
        );
    }
}
