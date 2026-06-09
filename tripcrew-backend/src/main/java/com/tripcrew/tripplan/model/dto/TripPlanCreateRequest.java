package com.tripcrew.tripplan.model.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 여행계획 생성 요청. owner 는 인증 주체(JWT)로 정하므로 본문에 받지 않는다.
 * 날짜는 선택값이며, 둘 다 주어진 경우 end >= start 검증은 서비스에서 한다.
 */
public record TripPlanCreateRequest(

        @NotBlank @Size(max = 150)
        String title,

        String description,

        LocalDate startDate,

        LocalDate endDate
) {
}
