package com.tripcrew.activity.model.dto;

import java.time.LocalDateTime;

public record UserActivityResponse(
        Long id,
        String activityType,
        Long tripPlanId,
        String tripPlanTitle,
        String placeName,
        Integer visitDay,
        LocalDateTime createdAt
) {
    public static UserActivityResponse from(UserActivity activity) {
        return new UserActivityResponse(activity.getId(), activity.getActivityType(),
                activity.getTripPlanId(), activity.getTripPlanTitle(), activity.getPlaceName(),
                activity.getVisitDay(), activity.getCreatedAt());
    }
}
