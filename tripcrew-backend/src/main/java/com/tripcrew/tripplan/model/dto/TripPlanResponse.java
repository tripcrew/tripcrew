package com.tripcrew.tripplan.model.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 여행계획 응답. version 을 포함시켜 클라이언트가 다음 수정 요청에 되돌려 보낼 수 있게 한다.
 */
public record TripPlanResponse(
        Long id,
        Long ownerId,
        String title,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        Long viewCount,
        Long version,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static TripPlanResponse from(TripPlan plan) {
        return new TripPlanResponse(
                plan.getId(),
                plan.getOwnerId(),
                plan.getTitle(),
                plan.getDescription(),
                plan.getStartDate(),
                plan.getEndDate(),
                plan.getViewCount(),
                plan.getVersion(),
                plan.getCreatedAt(),
                plan.getUpdatedAt()
        );
    }
}
