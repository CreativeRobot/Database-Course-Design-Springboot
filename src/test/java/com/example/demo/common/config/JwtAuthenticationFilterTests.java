package com.example.demo.common.config;

import com.example.demo.common.utils.JwtUtils;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import tools.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JwtAuthenticationFilterTests {
    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void authenticatesActiveUserAndKeepsControllerRequestAttributes() throws ServletException, IOException {
        JwtUtils jwtUtils = mock(JwtUtils.class);
        UserRepository userRepository = mock(UserRepository.class);
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(jwtUtils, userRepository, newWriter());
        User user = User.builder().id(7L).username("alice").role(Role.CUSTOMER).status(1).build();
        Claims claims = mock(Claims.class);
        when(claims.get("userId")).thenReturn(7L);
        when(claims.getExpiration()).thenReturn(new Date(System.currentTimeMillis() + 60_000));
        when(jwtUtils.parseToken("token")).thenReturn(claims);
        when(userRepository.findById(7L)).thenReturn(Optional.of(user));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/orders");
        request.addHeader("Authorization", "Bearer token");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);
        filter.doFilter(request, response, chain);
        assertTrue(response.getStatus() < 400);
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(user, request.getAttribute("user"));
        assertEquals(7L, request.getAttribute("userId"));
        assertEquals("alice", request.getAttribute("username"));
        assertEquals("CUSTOMER", request.getAttribute("role"));
        verify(chain).doFilter(request, response);
    }

    @Test
    void rejectsMissingTokenForProtectedRequest() throws ServletException, IOException {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(
                mock(JwtUtils.class), mock(UserRepository.class), newWriter());
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/orders");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);
        filter.doFilter(request, response, chain);
        assertEquals(401, response.getStatus());
        assertEquals("Bearer", response.getHeader("WWW-Authenticate"));
        assertEquals("no-store", response.getHeader("Cache-Control"));
        assertTrue(response.getContentAsString().contains("\"code\":401"));
        assertTrue(response.getContentAsString().contains("\"data\":null"));
        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void allowsAnonymousRecommendationGetRequestWithoutToken() throws ServletException, IOException {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(
                mock(JwtUtils.class), mock(UserRepository.class), newWriter());
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/recommendations/home");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);
        filter.doFilter(request, response, chain);
        verify(chain).doFilter(request, response);
        assertEquals(200, response.getStatus());
    }

    @Test
    void allowsPublicBookGetRequestWithoutToken() throws ServletException, IOException {
        JwtAuthenticationFilter filter = new JwtAuthenticationFilter(
                mock(JwtUtils.class), mock(UserRepository.class), newWriter());
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/books/12");
        MockHttpServletResponse response = new MockHttpServletResponse();
        FilterChain chain = mock(FilterChain.class);
        filter.doFilter(request, response, chain);
        verify(chain).doFilter(request, response);
        assertEquals(200, response.getStatus());
    }

    private SecurityErrorResponseWriter newWriter() {
        return new SecurityErrorResponseWriter(new ObjectMapper());
    }
}
