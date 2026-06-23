package com.tripcrew.tripplan.model.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.tripcrew.tripplan.model.TripMemberRole;

/**
 * 여행계획 응답. version 을 포함시켜 클라이언트가 다음 수정 요청에 되돌려 보낼 수 있게 한다.
 * myRole 은 요청자의 계획 내 역할(OWNER/EDITOR/VIEWER)로, 프론트가 편집 UI 노출 여부를 정하는 데 쓴다.
 * (역할을 모르는 호출 경로에서는 null 일 수 있다.)
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
        TripMemberRole myRole,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static TripPlanResponse from(TripPlan plan) {
        return from(plan, null);
    }

    public static TripPlanResponse from(TripPlan plan, TripMemberRole myRole) {
        return new TripPlanResponse(
                plan.getId(),
                plan.getOwnerId(),
                plan.getTitle(),
                plan.getDescription(),
                plan.getStartDate(),
                plan.getEndDate(),
                plan.getViewCount(),
                plan.getVersion(),
                myRole,
                plan.getCreatedAt(),
                plan.getUpdatedAt()
        );
    }
}
