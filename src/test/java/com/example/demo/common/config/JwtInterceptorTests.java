package com.example.demo.common.config;

import com.example.demo.common.utils.JwtUtils;
import com.example.demo.repository.UserRepository;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class JwtInterceptorTests {
    @Test
    void allowsUnauthenticatedCaptchaGetRequest() throws Exception {
        JwtInterceptor interceptor = newInterceptor();
        assertTrue(interceptor.preHandle(
                new MockHttpServletRequest("GET", "/api/auth/captcha"),
                new MockHttpServletResponse(),
                new Object()));
    }

    @Test
    void rejectsMissingTokenWithTheStandardUnauthorizedResponse() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();
        assertFalse(newInterceptor().preHandle(
                new MockHttpServletRequest("GET", "/api/orders"), response, new Object()));
        assertEquals(401, response.getStatus());
        assertEquals("Bearer", response.getHeader("WWW-Authenticate"));
        assertEquals("no-store", response.getHeader("Cache-Control"));
        assertTrue(response.getContentAsString().contains("\"code\":401"));
        assertTrue(response.getContentAsString().contains("\"data\":null"));
    }

    private JwtInterceptor newInterceptor() {
        JwtInterceptor interceptor = new JwtInterceptor();
        ReflectionTestUtils.setField(interceptor, "jwtUtils", mock(JwtUtils.class));
        ReflectionTestUtils.setField(interceptor, "userRepository", mock(UserRepository.class));
        ReflectionTestUtils.setField(interceptor, "securityErrorResponseWriter", new SecurityErrorResponseWriter(new ObjectMapper()));
        return interceptor;
    }
}
