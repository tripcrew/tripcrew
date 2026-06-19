package com.tripcrew.tripplan.service;

import java.math.BigDecimal;
import java.net.URI;
import java.util.OptionalLong;

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

        URI uri = UriComponentsBuilder.fromHttpUrl(directionsUrl)
                .queryParam("start", coordinate(startLongitude, startLatitude))
                .queryParam("goal", coordinate(goalLongitude, goalLatitude))
                .queryParam("option", "trafast")
                .build(true)
                .toUri();

        JsonNode body;
        try {
            body = restClient.get()
                    .uri(uri)
                    .header("X-NCP-APIGW-API-KEY-ID", clientId)
                    .header("X-NCP-APIGW-API-KEY", clientSecret)
                    .header(HttpHeaders.ACCEPT, "application/json")
                    .retrieve()
                    .body(JsonNode.class);
        } catch (RestClientResponseException e) {
            throw new BusinessException(
                    HttpStatus.BAD_GATEWAY,
                    "네이버 Directions 호출에 실패했습니다. status=" + e.getStatusCode().value()
            );
        } catch (RestClientException e) {
            throw new BusinessException(HttpStatus.BAD_GATEWAY, "네이버 Directions 호출에 실패했습니다.");
        }

        return extractDuration(body)
                .orElseThrow(() -> new BusinessException(
                        HttpStatus.BAD_GATEWAY,
                        "네이버 Directions 응답에서 경로 시간을 확인하지 못했습니다."
                ));
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
