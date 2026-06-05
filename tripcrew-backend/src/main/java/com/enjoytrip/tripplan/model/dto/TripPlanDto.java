package com.enjoytrip.tripplan.model.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TripPlanDto {

    private int planId;
    private String userId;
    private String title;
    private String createdAt;
    private List<TripPlanSpotDto> spots = new ArrayList<>();

    public int getId() {
        return planId;
    }

    public void setId(int id) {
        this.planId = id;
    }

    public String getPlanName() {
        return title;
    }

    public void setPlanName(String planName) {
        this.title = planName;
    }

    public void setSpots(List<TripPlanSpotDto> spots) {
        this.spots = spots == null ? new ArrayList<>() : spots;
    }

    public List<TripPlanSpotDto> getCourse() {
        return spots;
    }

    public void setCourse(List<TripPlanSpotDto> course) {
        setSpots(course);
    }
}
