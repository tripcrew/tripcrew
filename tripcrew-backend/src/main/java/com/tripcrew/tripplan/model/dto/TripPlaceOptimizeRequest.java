package com.tripcrew.tripplan.model.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record TripPlaceOptimizeRequest(
        @NotNull @Min(1)
        Integer visitDay
) {
}
