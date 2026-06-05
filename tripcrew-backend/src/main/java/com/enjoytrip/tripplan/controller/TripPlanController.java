package com.enjoytrip.tripplan.controller;

import com.enjoytrip.member.model.dto.MemberDto;
import com.enjoytrip.tripplan.model.dto.TripPlanDto;
import com.enjoytrip.tripplan.service.TripPlanService;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping({"/trip-plans", "/tripplan"})
public class TripPlanController {

    private final TripPlanService tripPlanService;

    public TripPlanController(TripPlanService tripPlanService) {
        this.tripPlanService = tripPlanService;
    }

    @GetMapping
    public ResponseEntity<List<TripPlanDto>> getTripPlans(@RequestParam(required = false) String userId, HttpSession session) {
        String resolvedUserId = resolveUserId(userId, session);
        if (resolvedUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(tripPlanService.getTripPlans(resolvedUserId));
    }

    @GetMapping("/{planId}")
    public ResponseEntity<TripPlanDto> getTripPlanDetail(@PathVariable int planId) {
        return ResponseEntity.ok(tripPlanService.getTripPlanDetail(planId));
    }

    @PostMapping
    public ResponseEntity<?> createTripPlan(@RequestBody TripPlanDto tripPlanDto, HttpSession session) {
        String resolvedUserId = resolveUserId(tripPlanDto.getUserId(), session);
        if (resolvedUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        tripPlanDto.setUserId(resolvedUserId);
        tripPlanService.createTripPlan(tripPlanDto);
        return ResponseEntity.ok(tripPlanDto);
    }

    @PostMapping(params = "action=delete")
    public ResponseEntity<Void> deleteTripPlanLegacy(@RequestBody TripPlanDto tripPlanDto) {
        tripPlanService.deleteTripPlan(tripPlanDto.getPlanId());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{planId}")
    public ResponseEntity<Integer> updateTripPlan(@PathVariable int planId, @RequestBody TripPlanDto tripPlanDto) {
        tripPlanDto.setPlanId(planId);
        return ResponseEntity.ok(tripPlanService.updateTripPlan(tripPlanDto));
    }

    @DeleteMapping("/{planId}")
    public ResponseEntity<Integer> deleteTripPlan(@PathVariable int planId) {
        return ResponseEntity.ok(tripPlanService.deleteTripPlan(planId));
    }

    private String resolveUserId(String userId, HttpSession session) {
        if (userId != null && !userId.isBlank()) {
            return userId;
        }
        Object loginUser = session.getAttribute("loginUser");
        if (loginUser instanceof MemberDto memberDto) {
            return memberDto.getUserId();
        }
        return null;
    }
}
