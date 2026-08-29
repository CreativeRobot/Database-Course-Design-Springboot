package com.example.demo.service;

import com.example.demo.entity.BookOrder;
import com.example.demo.entity.OrderStatus;
import com.example.demo.entity.User;
import com.example.demo.repository.BookOrderRepository;
import com.example.demo.repository.OrderItemRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.vo.OrderVo;
import com.example.demo.vo.PageVo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderQueryServiceTests {

    @Mock
    private BookOrderRepository bookOrderRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private UserRepository userRepository;

    @Test
    void listUserOrdersMapsOrdersAndItems() {
        User user = User.builder().id(7L).username("buyer").build();
        BookOrder order = BookOrder.builder()
                .id(30L)
                .user(user)
                .status(OrderStatus.COMPLETED)
                .build();
        when(userRepository.findByIdAndStatus(7L, 1))
                .thenReturn(Optional.of(User.builder().id(7L).status(1).build()));
        when(bookOrderRepository.findByUser_IdOrderByCreateTimeDesc(eq(7L), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(order)));
        when(orderItemRepository.findByOrder_IdOrderByIdAsc(30L)).thenReturn(List.of());

        PageVo<OrderVo> result = new OrderQueryService(bookOrderRepository, orderItemRepository, userRepository)
                .listUserOrders(7L, null, 1, 10);

        assertEquals(1, result.getTotal());
        assertEquals(30L, result.getRecords().getFirst().getId());
        assertEquals(OrderStatus.COMPLETED, result.getRecords().getFirst().getStatus());
    }

    @Test
    void getUserOrderRejectsOrderOwnedByAnotherUser() {
        when(userRepository.findByIdAndStatus(7L, 1))
                .thenReturn(Optional.of(User.builder().id(7L).status(1).build()));
        when(bookOrderRepository.findByIdAndUser_Id(30L, 7L)).thenReturn(Optional.empty());

        OrderQueryService service = new OrderQueryService(bookOrderRepository, orderItemRepository, userRepository);

        assertThrows(RuntimeException.class, () -> service.getUserOrder(7L, 30L));
    }
}
