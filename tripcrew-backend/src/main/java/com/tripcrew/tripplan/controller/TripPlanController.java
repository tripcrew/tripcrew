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

import com.tripcrew.tripplan.model.dto.TripPlanCreateRequest;
import com.tripcrew.tripplan.model.dto.TripPlanResponse;
import com.tripcrew.tripplan.model.dto.TripPlanUpdateRequest;
import com.tripcrew.tripplan.service.TripPlanService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * F03 여행계획 CRUD. 모든 엔드포인트는 인증 필요(SecurityConfig 의 anyRequest().authenticated()).
 * 수정/삭제는 소유자만 가능하며, 수정은 낙관적 락(version)으로 동시성 충돌을 막는다.
 */
@RestController
@RequestMapping("/api/trip-plans")
@RequiredArgsConstructor
public class TripPlanController {

    private final TripPlanService tripPlanService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TripPlanResponse create(@AuthenticationPrincipal Long userId,
                                   @Valid @RequestBody TripPlanCreateRequest request) {
        return tripPlanService.create(userId, request);
    }

    @GetMapping
    public List<TripPlanResponse> listMine(@AuthenticationPrincipal Long userId) {
        return tripPlanService.listMine(userId);
    }

    @GetMapping("/{id}")
    public TripPlanResponse get(@AuthenticationPrincipal Long userId, @PathVariable Long id) {
        return tripPlanService.get(id, userId);
    }

    @PutMapping("/{id}")
    public TripPlanResponse update(@AuthenticationPrincipal Long userId,
                                   @PathVariable Long id,
                                   @Valid @RequestBody TripPlanUpdateRequest request) {
        return tripPlanService.update(id, userId, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Long userId, @PathVariable Long id) {
        tripPlanService.delete(id, userId);
    }
}
