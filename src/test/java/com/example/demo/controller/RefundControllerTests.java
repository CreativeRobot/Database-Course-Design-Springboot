package com.example.demo.controller;

import com.example.demo.dto.ReviewRefundDTO;
import com.example.demo.entity.RefundRequest;
import com.example.demo.service.RefundService;
import com.example.demo.vo.PageVo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RefundControllerTests {
    @Mock private RefundService refundService;
    @InjectMocks private AdminRefundController refundController;

    @Test
    void forwardsAdminReview() {
        ReviewRefundDTO dto = new ReviewRefundDTO();
        refundController.review(21L, 99L, dto);
        verify(refundService).review(21L, 99L, dto);
    }
}


