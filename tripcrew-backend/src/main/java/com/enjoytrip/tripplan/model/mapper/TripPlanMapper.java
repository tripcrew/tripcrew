package com.enjoytrip.tripplan.model.mapper;

import com.enjoytrip.tripplan.model.dto.TripPlanDto;
import com.enjoytrip.tripplan.model.dto.TripPlanSpotDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TripPlanMapper {
    List<TripPlanDto> getTripPlans(@Param("userId") String userId);

    TripPlanDto getTripPlanDetail(@Param("planId") int planId);

    List<TripPlanSpotDto> getTripPlanSpots(@Param("planId") int planId);

    int createTripPlan(TripPlanDto tripPlanDto);

    int createTripPlanSpots(List<TripPlanSpotDto> spots);

    int updateTripPlan(TripPlanDto tripPlanDto);

    int deleteTripPlanSpots(@Param("planId") int planId);

    int deleteTripPlan(@Param("planId") int planId);
}
