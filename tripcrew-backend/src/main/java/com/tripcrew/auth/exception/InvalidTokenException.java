package com.tripcrew.auth.exception;

import org.springframework.http.HttpStatus;

import com.tripcrew.common.exception.BusinessException;

public class InvalidTokenException extends BusinessException {

    public InvalidTokenException() {
        super(HttpStatus.UNAUTHORIZED, "유효하지 않거나 만료된 토큰입니다.");
    }
}
