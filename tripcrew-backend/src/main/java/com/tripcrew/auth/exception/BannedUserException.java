package com.tripcrew.auth.exception;

import org.springframework.http.HttpStatus;

import com.tripcrew.common.exception.BusinessException;

/**
 * 제재(밴)된 계정의 로그인·토큰 재발급 시도. 403.
 */
public class BannedUserException extends BusinessException {

    public BannedUserException() {
        super(HttpStatus.FORBIDDEN, "제재된 계정입니다. 관리자에게 문의하세요.");
    }
}
