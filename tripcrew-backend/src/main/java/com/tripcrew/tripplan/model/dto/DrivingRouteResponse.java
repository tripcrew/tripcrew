package com.tripcrew.tripplan.model.dto;

import java.util.List;

/** 지도에 실제 도로 경로를 그리기 위한 Directions 좌표 응답. */
public record DrivingRouteResponse(List<RoutePoint> path) {

    public record RoutePoint(double latitude, double longitude) {
    }
}
