package com.example.demo.common.config;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityErrorResponseWriterTests {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final SecurityErrorResponseWriter writer =
            new SecurityErrorResponseWriter(objectMapper);

    @Test
    void writesUnauthorizedResultAndBearerHeaders() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        writer.write(response, HttpStatus.UNAUTHORIZED, "未登录或Token已失效");

        assertEquals(401, response.getStatus());
        assertEquals("Bearer", response.getHeader("WWW-Authenticate"));
        assertEquals("no-store", response.getHeader("Cache-Control"));
        JsonNode body = objectMapper.readTree(response.getContentAsString());
        assertEquals(401, body.path("code").asInt());
        assertEquals("未登录或Token已失效", body.path("message").asText());
        assertTrue(body.path("data").isNull());
    }

    @Test
    void writesForbiddenResultWithoutAuthenticationChallenge() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        writer.write(response, HttpStatus.FORBIDDEN, "无权访问");

        assertEquals(403, response.getStatus());
        assertNull(response.getHeader("WWW-Authenticate"));
        assertNull(response.getHeader("Cache-Control"));
        JsonNode body = objectMapper.readTree(response.getContentAsString());
        assertEquals(403, body.path("code").asInt());
        assertEquals("无权访问", body.path("message").asText());
        assertTrue(body.path("data").isNull());
    }
}
