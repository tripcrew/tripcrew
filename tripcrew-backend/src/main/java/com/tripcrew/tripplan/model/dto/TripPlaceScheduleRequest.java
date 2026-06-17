package com.tripcrew.tripplan.model.dto;

import jakarta.validation.constraints.Min;

/**
 * 후보 장소를 특정 Day 에 배치하거나 다시 후보 보관함으로 되돌리는 요청.
 * visitDay 가 null 이면 미배치 상태가 된다.
 */
public record TripPlaceScheduleRequest(
        @Min(1)
        Integer visitDay,

        @Min(1)
        Integer orderIndex
) {
}
