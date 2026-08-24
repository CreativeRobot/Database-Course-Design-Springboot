package com.example.demo.service;

import com.example.demo.common.exception.BusinessException;
import com.example.demo.common.utils.JwtUtils;
import com.example.demo.dto.LoginDTO;
import com.example.demo.dto.RegisterDTO;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTests {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private CaptchaService captchaService;

    @InjectMocks
    private AuthService authService;

    @Test
    void loginVerifiesCaptchaBeforeLookingUpUser() {
        LoginDTO request = new LoginDTO();
        request.setUsername("reader");
        request.setPassword("password");
        request.setCaptchaId("captcha-id");
        request.setCaptchaCode("abcde");
        doThrow(new BusinessException(org.springframework.http.HttpStatus.BAD_REQUEST, "验证码错误或已过期"))
                .when(captchaService).verifyAndConsume("captcha-id", "abcde");

        assertThrows(BusinessException.class, () -> authService.login(request));

        verify(captchaService).verifyAndConsume("captcha-id", "abcde");
        verify(userRepository, never()).findByUsernameIgnoreCase(any());
    }

    @Test
    void registerVerifiesCaptchaBeforeCheckingDuplicateUsername() {
        RegisterDTO request = new RegisterDTO();
        request.setUsername("reader");
        request.setPassword("password");
        request.setCaptchaId("captcha-id");
        request.setCaptchaCode("abcde");
        doThrow(new BusinessException(org.springframework.http.HttpStatus.BAD_REQUEST, "验证码错误或已过期"))
                .when(captchaService).verifyAndConsume("captcha-id", "abcde");

        assertThrows(BusinessException.class, () -> authService.register(request));

        verify(captchaService).verifyAndConsume("captcha-id", "abcde");
        verify(userRepository, never()).existsByUsernameIgnoreCase(any());
    }

    @Test
    void validCaptchaKeepsExistingLoginFlow() {
        LoginDTO request = new LoginDTO();
        request.setUsername("reader");
        request.setPassword("password");
        request.setCaptchaId("captcha-id");
        request.setCaptchaCode("abcde");
        User user = User.builder()
                .id(1L)
                .username("reader")
                .password("encoded")
                .status(1)
                .role(Role.CUSTOMER)
                .build();
        when(userRepository.findByUsernameIgnoreCase("reader")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("password", "encoded")).thenReturn(true);
        when(jwtUtils.generateToken(1L, "reader", "CUSTOMER")).thenReturn("token");

        authService.login(request);

        verify(captchaService).verifyAndConsume("captcha-id", "abcde");
        verify(userRepository).findByUsernameIgnoreCase("reader");
    }
}
