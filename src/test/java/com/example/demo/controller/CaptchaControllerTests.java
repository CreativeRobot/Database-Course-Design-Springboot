package com.example.demo.controller;

import com.example.demo.service.CaptchaService;
import com.example.demo.vo.CaptchaVo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CaptchaControllerTests {

    @Test
    void returnsIssuedCaptcha() {
        CaptchaService service = mock(CaptchaService.class);
        CaptchaVo captcha = new CaptchaVo("id", "iVBORw0KGgo=", 120);
        when(service.issue()).thenReturn(captcha);

        CaptchaController controller = new CaptchaController(service);

        var result = controller.getCaptcha();

        assertEquals(200, result.getCode());
        assertNotNull(result.getData());
        assertEquals("id", result.getData().getCaptchaId());
    }
}
