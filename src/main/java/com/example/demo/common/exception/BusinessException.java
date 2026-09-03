package com.example.demo.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * BusinessException 公共组件，提供后端各模块共享的基础能力。
 */
@Getter
public class BusinessException extends RuntimeException {
    private final HttpStatus status;

    public BusinessException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }
}