package com.tripcrew.tripplan.model.dto;

import java.util.List;

/** 지도에 실제 도로 경로와 Directions 교통 혼잡도 구간을 그리기 위한 응답. */
public record DrivingRouteResponse(List<RoutePoint> path, List<RouteSegment> segments, RouteSummary summary) {

    public record RoutePoint(double latitude, double longitude) {
    }

    /** congestion: 0=정보 없음, 1=원활, 2=서행, 3=혼잡. */
    public record RouteSegment(List<RoutePoint> path, int congestion) {
    }

    /** Directions가 산출한 승용차 기준 거리·시간·통행료·유류비 합계. */
    public record RouteSummary(long distanceMeter, long durationMillis, long tollFare, long fuelPrice) {
    }
}
