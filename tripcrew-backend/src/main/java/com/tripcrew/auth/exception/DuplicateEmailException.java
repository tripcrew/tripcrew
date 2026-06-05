package com.tripcrew.auth.exception;

import org.springframework.http.HttpStatus;

import com.tripcrew.common.exception.BusinessException;

public class DuplicateEmailException extends BusinessException {

    public DuplicateEmailException() {
        super(HttpStatus.CONFLICT, "이미 사용 중인 이메일입니다.");
    }
}
