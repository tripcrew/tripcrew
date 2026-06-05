package com.tripcrew.common.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

/**
 * 비즈니스 규칙 위반 예외의 베이스. HTTP 상태를 함께 들고 다닌다.
 * GlobalExceptionHandler 가 이 status/message 로 응답을 만든다.
 */
@Getter
public class BusinessException extends RuntimeException {

    private final HttpStatus status;

    public BusinessException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }
}
