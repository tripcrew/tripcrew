package com.tripcrew.tripplan.exception;

import org.springframework.http.HttpStatus;

import com.tripcrew.common.exception.BusinessException;

/** 역할 변경/제거 대상 멤버가 해당 계획에 없을 때. */
public class TripMemberNotFoundException extends BusinessException {

    public TripMemberNotFoundException() {
        super(HttpStatus.NOT_FOUND, "해당 계획의 멤버가 아닙니다.");
    }
}
