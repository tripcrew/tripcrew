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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.tripcrew.coedit.dto.PlaceChangeAction;
import com.tripcrew.coedit.edit.PlaceChangeBroadcaster;
import com.tripcrew.tripplan.model.dto.TripPlaceCreateRequest;
import com.tripcrew.tripplan.model.dto.DrivingRouteResponse;
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
    // F06 P2a — 장소 변경을 같은 계획 편집 중인 다른 접속자에게 실시간 알림(트랜잭션 커밋 후 호출).
    private final PlaceChangeBroadcaster placeChangeBroadcaster;

    @GetMapping
    public List<TripPlaceResponse> list(@AuthenticationPrincipal Long userId,
                                        @PathVariable Long planId) {
        return tripPlaceService.list(planId, userId);
    }

    @GetMapping("/driving-route")
    public DrivingRouteResponse drivingRoute(@AuthenticationPrincipal Long userId,
                                             @PathVariable Long planId,
                                             @RequestParam @jakarta.validation.constraints.Min(1) Integer visitDay) {
        return tripPlaceService.drivingRoute(planId, userId, visitDay);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TripPlaceResponse create(@AuthenticationPrincipal Long userId,
                                    @PathVariable Long planId,
                                    @Valid @RequestBody TripPlaceCreateRequest request) {
        TripPlaceResponse created = tripPlaceService.create(planId, userId, request);
        placeChangeBroadcaster.broadcast(planId, userId, PlaceChangeAction.ADDED);
        return created;
    }

    @PutMapping("/{placeId}/schedule")
    public TripPlaceResponse updateSchedule(@AuthenticationPrincipal Long userId,
                                            @PathVariable Long planId,
                                            @PathVariable Long placeId,
                                            @Valid @RequestBody TripPlaceScheduleRequest request) {
        TripPlaceResponse updated = tripPlaceService.updateSchedule(planId, placeId, userId, request);
        placeChangeBroadcaster.broadcast(planId, userId, PlaceChangeAction.SCHEDULED);
        return updated;
    }

    @PutMapping("/reorder")
    public List<TripPlaceResponse> reorder(@AuthenticationPrincipal Long userId,
                                           @PathVariable Long planId,
                                           @Valid @RequestBody TripPlaceReorderRequest request) {
        List<TripPlaceResponse> reordered = tripPlaceService.reorder(planId, userId, request);
        placeChangeBroadcaster.broadcast(planId, userId, PlaceChangeAction.REORDERED);
        return reordered;
    }

    @PostMapping("/optimize")
    public List<TripPlaceResponse> optimize(@AuthenticationPrincipal Long userId,
                                            @PathVariable Long planId,
                                            @Valid @RequestBody TripPlaceOptimizeRequest request) {
        List<TripPlaceResponse> optimized = tripPlaceService.optimize(planId, userId, request);
        placeChangeBroadcaster.broadcast(planId, userId, PlaceChangeAction.OPTIMIZED);
        return optimized;
    }

    @DeleteMapping("/{placeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Long userId,
                       @PathVariable Long planId,
                       @PathVariable Long placeId) {
        tripPlaceService.delete(planId, placeId, userId);
        placeChangeBroadcaster.broadcast(planId, userId, PlaceChangeAction.REMOVED);
    }
}
