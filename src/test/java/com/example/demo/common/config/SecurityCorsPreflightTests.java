package com.example.demo.common.config;

import com.example.demo.DemoApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@SpringBootTest(
        classes = DemoApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.MOCK,
        properties = {
                "spring.profiles.active=dev",
                "spring.datasource.url=jdbc:mysql://localhost:3306/bookstore?serverTimezone=Asia/Shanghai&useSSL=false&useUnicode=true&characterEncoding=utf8",
                "spring.datasource.username=root",
                "spring.datasource.password=123456",
                "jwt.secret=bookstore-dev-secret-key-2026-abcdef"
        })
class SecurityCorsPreflightTests {
    @Autowired
    private WebApplicationContext context;

    @Autowired
    @Qualifier("springSecurityFilterChain")
    private FilterChainProxy springSecurityFilterChain;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = webAppContextSetup(context).addFilters(springSecurityFilterChain).build();
    }

    @Test
    void allowsBrowserPreflightForProtectedApiRequests() throws Exception {
        mockMvc.perform(options("/api/recommendations/home")
                        .header(HttpHeaders.ORIGIN, "http://127.0.0.1:9100")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, HttpMethod.GET.name())
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "authorization,content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "http://127.0.0.1:9100"));
    }
}
