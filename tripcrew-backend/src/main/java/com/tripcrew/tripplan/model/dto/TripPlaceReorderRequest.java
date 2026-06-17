package com.tripcrew.tripplan.model.dto;

import java.util.List;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;

public record TripPlaceReorderRequest(
        @Min(1)
        Integer visitDay,

        @NotEmpty
        List<Long> placeIds
) {
}
