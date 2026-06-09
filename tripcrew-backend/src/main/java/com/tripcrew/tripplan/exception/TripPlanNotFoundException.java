package com.tripcrew.tripplan.exception;

import org.springframework.http.HttpStatus;

import com.tripcrew.common.exception.BusinessException;

public class TripPlanNotFoundException extends BusinessException {

    public TripPlanNotFoundException() {
        super(HttpStatus.NOT_FOUND, "여행계획을 찾을 수 없습니다.");
    }
}
