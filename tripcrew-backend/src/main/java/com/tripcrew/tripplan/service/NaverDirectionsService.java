package com.tripcrew.tripplan.service;

import java.math.BigDecimal;
import java.net.URI;
import java.util.OptionalLong;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.tripcrew.common.exception.BusinessException;
import com.tripcrew.tripplan.model.dto.DrivingRouteResponse.RoutePoint;
import com.tripcrew.tripplan.model.dto.DrivingRouteResponse;
import com.tripcrew.tripplan.model.dto.DrivingRouteResponse.RouteSegment;
import com.tripcrew.tripplan.model.dto.DrivingRouteResponse.RouteSummary;

@Service
public class NaverDirectionsService {

    private final RestClient restClient;
    private final String clientId;
    private final String clientSecret;
    private final String directionsUrl;

    public NaverDirectionsService(
            RestClient.Builder restClientBuilder,
            @Value("${naver.maps.client-id}") String clientId,
            @Value("${naver.maps.client-secret}") String clientSecret,
            @Value("${naver.maps.directions-url}") String directionsUrl
    ) {
        this.restClient = restClientBuilder.build();
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.directionsUrl = directionsUrl;
    }

    public boolean isConfigured() {
        return hasText(clientId) && hasText(clientSecret);
    }

    public long drivingDurationMillis(BigDecimal startLatitude,
                                      BigDecimal startLongitude,
                                      BigDecimal goalLatitude,
                                      BigDecimal goalLongitude) {
        if (!isConfigured()) {
            throw new IllegalStateException("NAVER Directions credentials are not configured.");
        }

        return extractDuration(requestDirections(startLatitude, startLongitude, goalLatitude, goalLongitude))
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.BAD_GATEWAY,
                        "네이버 Directions 응답에서 경로 시간을 확인하지 못했습니다."
                ));
    }

    /** Directions의 [경도, 위도] path를 지도 SDK가 쓸 [위도, 경도]로 변환한다. */
    public List<RoutePoint> drivingPath(BigDecimal startLatitude,
                                        BigDecimal startLongitude,
                                        BigDecimal goalLatitude,
                                        BigDecimal goalLongitude) {
        return drivingRoute(startLatitude, startLongitude, goalLatitude, goalLongitude).path();
    }

    public DrivingRouteResponse drivingRoute(BigDecimal startLatitude,
                                              BigDecimal startLongitude,
                                              BigDecimal goalLatitude,
                                              BigDecimal goalLongitude) {
        JsonNode body = requestDirections(startLatitude, startLongitude, goalLatitude, goalLongitude);
        JsonNode path = body == null
                ? null
                : body.path("route").path("trafast").path(0).path("path");
        List<RoutePoint> points = new ArrayList<>();
        if (path != null && path.isArray()) {
            for (JsonNode coordinate : path) {
                if (coordinate.size() >= 2 && coordinate.get(0).isNumber() && coordinate.get(1).isNumber()) {
                    points.add(new RoutePoint(coordinate.get(1).asDouble(), coordinate.get(0).asDouble()));
                }
            }
        }
        if (points.size() < 2) {
            throw new BusinessException(HttpStatus.BAD_GATEWAY, "네이버 Directions 응답에서 경로 좌표를 확인하지 못했습니다.");
        }
        return new DrivingRouteResponse(points, trafficSegments(body, points), routeSummary(body));
    }

    private RouteSummary routeSummary(JsonNode body) {
        JsonNode summary = body == null
                ? null
                : body.path("route").path("trafast").path(0).path("summary");
        if (summary == null || summary.isMissingNode()) {
            return new RouteSummary(0, 0, 0, 0);
        }
        return new RouteSummary(
                summary.path("distance").asLong(0),
                summary.path("duration").asLong(0),
                summary.path("tollFare").asLong(0),
                summary.path("fuelPrice").asLong(0)
        );
    }

    private List<RouteSegment> trafficSegments(JsonNode body, List<RoutePoint> points) {
        JsonNode sections = body == null
                ? null
                : body.path("route").path("trafast").path(0).path("section");
        List<RouteSegment> segments = new ArrayList<>();
        if (sections == null || !sections.isArray()) {
            return segments;
        }

        for (JsonNode section : sections) {
            int startIndex = section.path("pointIndex").asInt(-1);
            int pointCount = section.path("pointCount").asInt(0);
            int endIndex = Math.min(points.size(), startIndex + pointCount);
            if (startIndex < 0 || endIndex - startIndex < 2) {
                continue;
            }
            segments.add(new RouteSegment(
                    new ArrayList<>(points.subList(startIndex, endIndex)),
                    section.path("congestion").asInt(0)
            ));
        }
        return segments;
    }

    private JsonNode requestDirections(BigDecimal startLatitude,
                                       BigDecimal startLongitude,
                                       BigDecimal goalLatitude,
                                       BigDecimal goalLongitude) {
        if (!isConfigured()) {
            throw new BusinessException(HttpStatus.SERVICE_UNAVAILABLE, "네이버 Directions 설정이 필요합니다.");
        }
        URI uri = UriComponentsBuilder.fromHttpUrl(directionsUrl)
                .queryParam("start", coordinate(startLongitude, startLatitude))
                .queryParam("goal", coordinate(goalLongitude, goalLatitude))
                .queryParam("option", "trafast")
                .build(true)
                .toUri();
        try {
            return restClient.get()
                    .uri(uri)
                    .header("X-NCP-APIGW-API-KEY-ID", clientId)
                    .header("X-NCP-APIGW-API-KEY", clientSecret)
                    .header(HttpHeaders.ACCEPT, "application/json")
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RestClientResponseException e) {
            throw new BusinessException(HttpStatus.BAD_GATEWAY,
                    "네이버 Directions 호출에 실패했습니다. status=" + e.getStatusCode().value());
        } catch (RestClientException e) {
            throw new BusinessException(HttpStatus.BAD_GATEWAY, "네이버 Directions 호출에 실패했습니다.");
        }
    }

    private OptionalLong extractDuration(JsonNode body) {
        JsonNode duration = body == null
                ? null
                : body.path("route").path("trafast").path(0).path("summary").path("duration");
        if (duration == null || !duration.canConvertToLong()) {
            return OptionalLong.empty();
        }
        return OptionalLong.of(duration.asLong());
    }

    private String coordinate(BigDecimal longitude, BigDecimal latitude) {
        return longitude.toPlainString() + "," + latitude.toPlainString();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
