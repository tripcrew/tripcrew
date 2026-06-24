package com.tripcrew.auth.exception;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.http.HttpStatus;

import com.tripcrew.common.exception.BusinessException;

/**
 * 신고 누적으로 계정이 임시 정지(ACCOUNT_SUSPEND)된 사용자의 로그인·토큰 재발급 시도. 403.
 * 영구 정지(BANNED)와 달리 해제 시각이 있으므로 함께 안내한다.
 */
public class SuspendedUserException extends BusinessException {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public SuspendedUserException(LocalDateTime until) {
        super(HttpStatus.FORBIDDEN, message(until));
    }

    private static String message(LocalDateTime until) {
        String suffix = until == null ? "" : " (해제: " + until.format(FMT) + ")";
        return "신고 누적으로 일시 정지된 계정입니다." + suffix;
    }
}
