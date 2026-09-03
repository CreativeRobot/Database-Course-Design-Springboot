package com.example.demo.controller;

import com.example.demo.dto.CreateRefundRequestDTO;
import com.example.demo.entity.RefundRequest;
import com.example.demo.service.RefundService;
import com.example.demo.vo.RefundRequestVo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class CustomerRefundControllerTests {
    @Mock private RefundService refundService;
    @InjectMocks private RefundController refundController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        mockMvc = MockMvcBuilders.standaloneSetup(refundController)
                .setValidator(validator)
                .build();
    }

    @Test
    void createsRefundWhenOrderIdComesFromPathInsteadOfRequestBody() throws Exception {
        RefundRequest saved = new RefundRequest();
        RefundRequestVo response = new RefundRequestVo();
        response.setId(31L);
        response.setOrderId(7L);
        when(refundService.createRequest(any(), any(CreateRefundRequestDTO.class))).thenAnswer(invocation -> {
            CreateRefundRequestDTO dto = invocation.getArgument(1);
            if (!Long.valueOf(7L).equals(dto.getOrderId())) {
                throw new AssertionError("path orderId was not copied into the refund request DTO");
            }
            return saved;
        });
        when(refundService.toVoForController(saved)).thenReturn(response);

        mockMvc.perform(post("/api/orders/7/refunds")
                        .requestAttr("userId", 21L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "orderItemId": 11,
                                  "type": "REFUND_ONLY",
                                  "quantity": 1,
                                  "reason": "商品破损"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.orderId").value(7));
    }
}
