package com.tripcrew.admin.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import com.tripcrew.admin.model.dto.ExternalApiHealthItem;
import com.tripcrew.admin.model.dto.ExternalApiHealthResponse;
import com.tripcrew.chat.service.ChatService;
import com.tripcrew.common.exception.BusinessException;
import com.tripcrew.tripplan.service.NaverDirectionsService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminExternalHealthService {

    private static final BigDecimal SEOUL_CITY_HALL_LAT = new BigDecimal("37.5666103");
    private static final BigDecimal SEOUL_CITY_HALL_LNG = new BigDecimal("126.9783882");
    private static final BigDecimal GWANGHWAMUN_LAT = new BigDecimal("37.5758770");
    private static final BigDecimal GWANGHWAMUN_LNG = new BigDecimal("126.9768121");

    private final ChatService chatService;
    private final NaverDirectionsService naverDirectionsService;

    public ExternalApiHealthResponse externalHealth(boolean live) {
        return new ExternalApiHealthResponse(
                Instant.now(),
                live
                        ? List.of(checkNaverDirections(), checkGemini())
                        : List.of(configuredNaverDirections(), configuredGemini())
        );
    }

    private ExternalApiHealthItem configuredNaverDirections() {
        if (!naverDirectionsService.isConfigured()) {
            return unavailable("naver-directions", "Naver Directions", "API 키가 설정되지 않았습니다.");
        }
        return configured("naver-directions", "Naver Directions");
    }

    private ExternalApiHealthItem configuredGemini() {
        if (!chatService.isGeminiConfigured()) {
            return unavailable("gemini", "Gemini", "API 키가 설정되지 않았습니다.");
        }
        return configured("gemini", "Gemini");
    }

    private ExternalApiHealthItem checkNaverDirections() {
        if (!naverDirectionsService.isConfigured()) {
            return unavailable("naver-directions", "Naver Directions", "API 키가 설정되지 않았습니다.");
        }
        long started = System.nanoTime();
        try {
            naverDirectionsService.drivingDurationMillis(
                    SEOUL_CITY_HALL_LAT,
                    SEOUL_CITY_HALL_LNG,
                    GWANGHWAMUN_LAT,
                    GWANGHWAMUN_LNG
            );
            return up("naver-directions", "Naver Directions", elapsedMs(started));
        } catch (BusinessException | RestClientException e) {
            return down("naver-directions", "Naver Directions", e.getMessage(), elapsedMs(started));
        }
    }

    private ExternalApiHealthItem checkGemini() {
        if (!chatService.isGeminiConfigured()) {
            return unavailable("gemini", "Gemini", "API 키가 설정되지 않았습니다.");
        }
        long started = System.nanoTime();
        try {
            chatService.checkGeminiHealth();
            return up("gemini", "Gemini", elapsedMs(started));
        } catch (BusinessException | RestClientException e) {
            return down("gemini", "Gemini", e.getMessage(), elapsedMs(started));
        }
    }

    private ExternalApiHealthItem up(String key, String name, long latencyMs) {
        return new ExternalApiHealthItem(key, name, "UP", "정상", latencyMs);
    }

    private ExternalApiHealthItem configured(String key, String name) {
        return new ExternalApiHealthItem(key, name, "CONFIGURED", "API 키 설정됨", null);
    }

    private ExternalApiHealthItem unavailable(String key, String name, String message) {
        return new ExternalApiHealthItem(key, name, "UNAVAILABLE", message, null);
    }

    private ExternalApiHealthItem down(String key, String name, String message, long latencyMs) {
        return new ExternalApiHealthItem(key, name, "DOWN", message, latencyMs);
    }

    private long elapsedMs(long started) {
        return (System.nanoTime() - started) / 1_000_000;
    }
}
