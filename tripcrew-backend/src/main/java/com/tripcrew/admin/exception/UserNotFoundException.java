package com.tripcrew.admin.exception;

import org.springframework.http.HttpStatus;

import com.tripcrew.common.exception.BusinessException;

public class UserNotFoundException extends BusinessException {

    public UserNotFoundException() {
        super(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다.");
    }
}
