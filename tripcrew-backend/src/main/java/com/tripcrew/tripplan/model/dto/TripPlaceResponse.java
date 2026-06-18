package com.tripcrew.tripplan.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record TripPlaceResponse(
        Long id,
        Long tripPlanId,
        Integer attractionId,
        String name,
        BigDecimal latitude,
        BigDecimal longitude,
        Integer visitDay,
        Integer orderIndex,
        String memo,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static TripPlaceResponse from(TripPlace place) {
        return new TripPlaceResponse(
                place.getId(),
                place.getTripPlanId(),
                place.getAttractionId(),
                place.getName(),
                place.getLatitude(),
                place.getLongitude(),
                place.getVisitDay(),
                place.getOrderIndex(),
                place.getMemo(),
                place.getCreatedAt(),
                place.getUpdatedAt()
        );
    }
}
