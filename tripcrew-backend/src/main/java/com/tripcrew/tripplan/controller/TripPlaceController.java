package com.tripcrew.tripplan.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.tripcrew.tripplan.model.dto.TripPlaceCreateRequest;
import com.tripcrew.tripplan.model.dto.TripPlaceOptimizeRequest;
import com.tripcrew.tripplan.model.dto.TripPlaceReorderRequest;
import com.tripcrew.tripplan.model.dto.TripPlaceResponse;
import com.tripcrew.tripplan.model.dto.TripPlaceScheduleRequest;
import com.tripcrew.tripplan.service.TripPlaceService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/trip-plans/{planId}/places")
@RequiredArgsConstructor
public class TripPlaceController {

    private final TripPlaceService tripPlaceService;

    @GetMapping
    public List<TripPlaceResponse> list(@AuthenticationPrincipal Long userId,
                                        @PathVariable Long planId) {
        return tripPlaceService.list(planId, userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TripPlaceResponse create(@AuthenticationPrincipal Long userId,
                                    @PathVariable Long planId,
                                    @Valid @RequestBody TripPlaceCreateRequest request) {
        return tripPlaceService.create(planId, userId, request);
    }

    @PutMapping("/{placeId}/schedule")
    public TripPlaceResponse updateSchedule(@AuthenticationPrincipal Long userId,
                                            @PathVariable Long planId,
                                            @PathVariable Long placeId,
                                            @Valid @RequestBody TripPlaceScheduleRequest request) {
        return tripPlaceService.updateSchedule(planId, placeId, userId, request);
    }

    @PutMapping("/reorder")
    public List<TripPlaceResponse> reorder(@AuthenticationPrincipal Long userId,
                                           @PathVariable Long planId,
                                           @Valid @RequestBody TripPlaceReorderRequest request) {
        return tripPlaceService.reorder(planId, userId, request);
    }

    @PostMapping("/optimize")
    public List<TripPlaceResponse> optimize(@AuthenticationPrincipal Long userId,
                                            @PathVariable Long planId,
                                            @Valid @RequestBody TripPlaceOptimizeRequest request) {
        return tripPlaceService.optimize(planId, userId, request);
    }

    @DeleteMapping("/{placeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Long userId,
                       @PathVariable Long planId,
                       @PathVariable Long placeId) {
        tripPlaceService.delete(planId, placeId, userId);
    }
}
