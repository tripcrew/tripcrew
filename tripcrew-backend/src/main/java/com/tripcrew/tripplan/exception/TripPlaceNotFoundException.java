package com.tripcrew.tripplan.exception;

import org.springframework.http.HttpStatus;

import com.tripcrew.common.exception.BusinessException;

public class TripPlaceNotFoundException extends BusinessException {

    public TripPlaceNotFoundException() {
        super(HttpStatus.NOT_FOUND, "여행계획 장소를 찾을 수 없습니다.");
    }
}
