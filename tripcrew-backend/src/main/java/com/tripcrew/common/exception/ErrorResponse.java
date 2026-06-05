package com.tripcrew.common.exception;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 표준 에러 응답 본문.
 * errors 는 유효성 검증 실패 시 필드별 메시지(없으면 직렬화에서 생략).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
        int status,
        String message,
        Map<String, String> errors
) {
    public static ErrorResponse of(int status, String message) {
        return new ErrorResponse(status, message, null);
    }

    public static ErrorResponse of(int status, String message, Map<String, String> errors) {
        return new ErrorResponse(status, message, errors);
    }
}
