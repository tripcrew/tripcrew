package com.tripcrew.tripplan.model.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 여행계획 수정 요청. 낙관적 락을 위해 클라이언트가 마지막으로 읽은 {@code version} 을
 * 반드시 함께 보낸다. 서버의 현재 version 과 다르면(다른 사용자가 먼저 수정) 409 충돌.
 */
public record TripPlanUpdateRequest(

        @NotBlank @Size(max = 150)
        String title,

        String description,

        LocalDate startDate,

        LocalDate endDate,

        @NotNull
        Long version
) {
}
