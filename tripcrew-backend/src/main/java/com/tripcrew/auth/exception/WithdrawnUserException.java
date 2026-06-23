package com.tripcrew.auth.exception;

import org.springframework.http.HttpStatus;

import com.tripcrew.common.exception.BusinessException;

public class WithdrawnUserException extends BusinessException {

    public WithdrawnUserException() {
        super(HttpStatus.FORBIDDEN, "탈퇴한 계정입니다.");
    }
}
