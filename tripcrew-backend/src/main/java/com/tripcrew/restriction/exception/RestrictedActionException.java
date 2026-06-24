package com.tripcrew.restriction.exception;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.http.HttpStatus;

import com.tripcrew.common.exception.BusinessException;
import com.tripcrew.restriction.model.RestrictionType;

/**
 * 능력 제재(후기/계획 작성 금지) 중인 사용자가 해당 작업을 시도했을 때. 403.
 * 어떤 작업이 언제까지 제한되는지 안내해 사용자가 맥락을 알 수 있게 한다.
 */
public class RestrictedActionException extends BusinessException {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public RestrictedActionException(RestrictionType type, LocalDateTime until) {
        super(HttpStatus.FORBIDDEN, message(type, until));
    }

    private static String message(RestrictionType type, LocalDateTime until) {
        String action = switch (type) {
            case REVIEW_WRITE -> "후기 작성";
            case PLAN_CREATE -> "여행계획 작성";
            case ACCOUNT_SUSPEND -> "계정 이용";
        };
        String suffix = until == null ? "" : " (해제: " + until.format(FMT) + ")";
        return "신고 누적으로 " + action + "이 제한된 상태입니다." + suffix;
    }
}
