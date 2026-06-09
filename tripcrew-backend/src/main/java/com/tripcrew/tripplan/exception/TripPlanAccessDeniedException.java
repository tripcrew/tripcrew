package com.tripcrew.tripplan.exception;

import org.springframework.http.HttpStatus;

import com.tripcrew.common.exception.BusinessException;

/** 소유자가 아닌 사용자가 수정/삭제를 시도. (F06 공동편집 권한은 추후 별도 처리) */
public class TripPlanAccessDeniedException extends BusinessException {

    public TripPlanAccessDeniedException() {
        super(HttpStatus.FORBIDDEN, "해당 여행계획에 대한 권한이 없습니다.");
    }
}
