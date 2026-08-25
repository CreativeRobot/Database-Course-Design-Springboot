package com.example.demo;

import com.example.demo.common.config.SecurityErrorResponseWriter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class DemoApplicationTests {

    @Autowired
    private SecurityErrorResponseWriter securityErrorResponseWriter;

    @Test
    void applicationContextProvidesSecurityErrorResponseWriter() {
        assertNotNull(securityErrorResponseWriter);
    }
}
