package com.enjoytrip.tripplan.model.dto;

import lombok.Data;

@Data
public class TripPlanSpotDto {

    private int spotId;
    private int planId;
    private int attractionId;
    private int order;
    private String title;
    private String addr;
    private double lat;
    private double lng;
    private String image;

    public int getId() {
        return attractionId;
    }

    public void setId(int id) {
        this.attractionId = id;
    }

    public int getSpotOrder() {
        return order;
    }

    public void setSpotOrder(int spotOrder) {
        this.order = spotOrder;
    }
}
