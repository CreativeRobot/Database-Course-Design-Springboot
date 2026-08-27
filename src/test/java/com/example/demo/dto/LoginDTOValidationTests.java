package com.example.demo.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginDTOValidationTests {

    private ValidatorFactory validatorFactory;
    private Validator validator;

    @BeforeEach
    void setUp() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterEach
    void tearDown() {
        if (validatorFactory != null) {
            validatorFactory.close();
        }
    }

    @Test
    void loginDTOAllowsBothCaptchaFieldsAbsent() {
        LoginDTO loginDTO = validLoginDTO();

        Set<ConstraintViolation<LoginDTO>> violations = validator.validate(loginDTO);

        assertFalse(hasViolationForProperty(violations, "captchaId"));
        assertFalse(hasViolationForProperty(violations, "captchaCode"));
    }

    @Test
    void registerDTOStillRequiresCaptchaFields() {
        RegisterDTO registerDTO = validRegisterDTO();

        Set<ConstraintViolation<RegisterDTO>> violations = validator.validate(registerDTO);

        assertTrue(hasViolationForProperty(violations, "captchaId"));
        assertTrue(hasViolationForProperty(violations, "captchaCode"));
    }

    private LoginDTO validLoginDTO() {
        LoginDTO loginDTO = new LoginDTO();
        loginDTO.setUsername("reader");
        loginDTO.setPassword("password");
        return loginDTO;
    }

    private RegisterDTO validRegisterDTO() {
        RegisterDTO registerDTO = new RegisterDTO();
        registerDTO.setUsername("reader");
        registerDTO.setPassword("password");
        registerDTO.setNickname("nick");
        registerDTO.setEmail("reader@example.com");
        registerDTO.setPhone("13800138000");
        return registerDTO;
    }

    private boolean hasViolationForProperty(
            Set<? extends ConstraintViolation<?>> violations,
            String propertyPath
    ) {
        return violations.stream()
                .anyMatch(violation -> propertyPath.equals(violation.getPropertyPath().toString()));
    }
}
