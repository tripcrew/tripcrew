package com.tripcrew.tripplan.model.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

/**
 * 장소 추가 요청. attractionId 가 있으면 관광지 스냅샷을 복사하고,
 * 없으면 name/latitude/longitude 로 커스텀 장소를 만든다.
 */
public record TripPlaceCreateRequest(
        Integer attractionId,

        @Size(max = 255)
        String name,

        BigDecimal latitude,

        BigDecimal longitude,

        @Min(1)
        Integer visitDay,

        @Size(max = 255)
        String memo
) {
}
