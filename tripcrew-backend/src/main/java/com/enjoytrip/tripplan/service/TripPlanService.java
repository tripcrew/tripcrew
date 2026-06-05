package com.enjoytrip.tripplan.service;

import com.enjoytrip.tripplan.model.dto.TripPlanDto;
import com.enjoytrip.tripplan.model.dto.TripPlanSpotDto;
import com.enjoytrip.tripplan.model.mapper.TripPlanMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TripPlanService {

    private final TripPlanMapper tripPlanMapper;

    public TripPlanService(TripPlanMapper tripPlanMapper) {
        this.tripPlanMapper = tripPlanMapper;
    }

    public List<TripPlanDto> getTripPlans(String userId) {
        List<TripPlanDto> plans = tripPlanMapper.getTripPlans(userId);
        for (TripPlanDto plan : plans) {
            TripPlanDto detail = tripPlanMapper.getTripPlanDetail(plan.getPlanId());
            if (detail != null) {
                plan.setSpots(detail.getSpots());
            }
        }
        return plans;
    }

    public TripPlanDto getTripPlanDetail(int planId) {
        return tripPlanMapper.getTripPlanDetail(planId);
    }

    @Transactional
    public int createTripPlan(TripPlanDto tripPlanDto) {
        tripPlanMapper.createTripPlan(tripPlanDto);
        int planId = tripPlanDto.getPlanId();

        List<TripPlanSpotDto> spots = tripPlanDto.getSpots();
        if (spots != null && !spots.isEmpty()) {
            applySpotMetadata(planId, spots);
            return tripPlanMapper.createTripPlanSpots(spots);
        }
        return 1;
    }

    @Transactional
    public int updateTripPlan(TripPlanDto tripPlanDto) {
        tripPlanMapper.updateTripPlan(tripPlanDto);
        int planId = tripPlanDto.getPlanId();

        tripPlanMapper.deleteTripPlanSpots(planId);

        List<TripPlanSpotDto> spots = tripPlanDto.getSpots();
        if (spots != null && !spots.isEmpty()) {
            applySpotMetadata(planId, spots);
            return tripPlanMapper.createTripPlanSpots(spots);
        }
        return 1;
    }

    @Transactional
    public int deleteTripPlan(int planId) {
        tripPlanMapper.deleteTripPlanSpots(planId);
        return tripPlanMapper.deleteTripPlan(planId);
    }

    private void applySpotMetadata(int planId, List<TripPlanSpotDto> spots) {
        for (int i = 0; i < spots.size(); i++) {
            TripPlanSpotDto spot = spots.get(i);
            spot.setPlanId(planId);
            spot.setOrder(i + 1);
        }
    }
}
