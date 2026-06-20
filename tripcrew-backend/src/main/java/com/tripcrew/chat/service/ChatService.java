package com.tripcrew.chat.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.tripcrew.chat.model.dto.ChatMessage;
import com.tripcrew.chat.model.dto.ChatMessageRequest;
import com.tripcrew.chat.model.dto.ChatMessageResponse;
import com.tripcrew.chat.model.mapper.ChatMessageMapper;
import com.tripcrew.common.exception.BusinessException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatService implements InitializingBean {

    private static final int MAX_GEMINI_ATTEMPTS = 3;

    private final ChatMessageMapper chatMessageMapper;
    private RestClient restClient;

    @Value("${gemini.api-key:}")
    private String apiKey;

    @Value("${gemini.model:gemini-2.5-flash-lite}")
    private String model;

    @Value("${gemini.base-url:https://generativelanguage.googleapis.com/v1beta}")
    private String baseUrl;

    @Override
    public void afterPropertiesSet() {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }

    @Transactional
    public ChatMessageResponse send(Long userId, ChatMessageRequest request) {
        save(userId, request.tripPlanId(), "USER", request.message());

        String answer = askGemini(request.message());

        save(userId, request.tripPlanId(), "ASSISTANT", answer);

        return new ChatMessageResponse(answer);

    }

    private String askGemini(String userMessage) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new BusinessException(HttpStatus.BAD_GATEWAY, "Gemini API 키가 설정되지 않았습니다.");
        }

        GeminiRequest request = new GeminiRequest(
                List.of(new GeminiContent("user", List.of(new GeminiPart(userMessage)))),
                new GeminiContent(null, List.of(new GeminiPart(systemInstruction()))),
                new GeminiGenerationConfig(0.7, 512)
        );

        GeminiResponse response = callGeminiWithRetry(request);

        String answer = extractOutputText(response);
        if (answer.isBlank()) {
            return "답변을 생성하지 못했어요. 여행 조건을 조금 더 구체적으로 입력해주세요.";
        }
        return answer;
    }

    private String generateContentUri() {
        return UriComponentsBuilder.fromPath("/models/{model}:generateContent")
                .buildAndExpand(model)
                .toUriString();
    }

    private GeminiResponse callGeminiWithRetry(GeminiRequest request) {
        RestClientResponseException lastException = null;
        for (int attempt = 1; attempt <= MAX_GEMINI_ATTEMPTS; attempt++) {
            try {
                return restClient.post()
                        .uri(generateContentUri())
                        .header("x-goog-api-key", apiKey)
                        .body(request)
                        .retrieve()
                        .body(GeminiResponse.class);
            } catch (RestClientResponseException e) {
                lastException = e;
                if (e.getStatusCode().value() != 503 || attempt == MAX_GEMINI_ATTEMPTS) {
                    throw toGeminiException(e);
                }
                sleepBeforeRetry(attempt);
            }
        }
        throw toGeminiException(lastException);
    }

    private void sleepBeforeRetry(int attempt) {
        try {
            Thread.sleep(600L * attempt);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(HttpStatus.BAD_GATEWAY, "Gemini 응답 대기 중 요청이 중단되었습니다.");
        }
    }

    private BusinessException toGeminiException(RestClientResponseException e) {
        int status = e.getStatusCode().value();
        log.warn("Gemini API error: status={}, body={}", status, truncate(e.getResponseBodyAsString()));
        if (status == 401 || status == 403) {
            return new BusinessException(HttpStatus.BAD_GATEWAY, "Gemini API 키 설정이 올바르지 않습니다.");
        }
        if (status == 429) {
            return new BusinessException(HttpStatus.TOO_MANY_REQUESTS, "Gemini 요청 한도에 걸렸습니다. 모델/키 상태를 확인하거나 잠시 후 다시 시도해주세요.");
        }
        if (status == 503) {
            return new BusinessException(HttpStatus.BAD_GATEWAY, "Gemini 모델이 일시적으로 혼잡합니다. 잠시 후 다시 시도해주세요.");
        }
        return new BusinessException(HttpStatus.BAD_GATEWAY, "Gemini 응답을 가져오지 못했습니다.");
    }

    private String truncate(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }
        return value.length() <= 500 ? value : value.substring(0, 500) + "...";
    }

    private String extractOutputText(GeminiResponse response) {
        return Optional.ofNullable(response)
                .map(GeminiResponse::candidates)
                .orElse(List.of())
                .stream()
                .map(GeminiCandidate::content)
                .filter(content -> content != null && content.parts() != null)
                .flatMap(content -> content.parts().stream())
                .map(GeminiPart::text)
                .filter(text -> text != null && !text.isBlank())
                .collect(Collectors.joining("\n\n"));
    }

    private String systemInstruction() {
        return """
                너는 TripCrew의 여행 추천 챗봇 TripBot이다.
                사용자의 여행 조건을 바탕으로 한국 여행 코스를 추천한다.
                답변은 한국어로 한다.
                너무 길게 설명하지 말고, 아래 형식을 지켜라.

                1. 요약
                2. 추천 코스
                3. 이동/주의사항

                실제 존재하지 않는 장소나 확실하지 않은 정보는 단정하지 마라.
                """;
    }

    private void save(Long userId, Long tripPlanId, String role, String content) {
        ChatMessage message = ChatMessage.builder()
                .userId(userId)
                .tripPlanId(tripPlanId)
                .role(role)
                .content(content)
                .build();

        chatMessageMapper.insert(message);
    }

    private record GeminiRequest(
            List<GeminiContent> contents,
            GeminiContent systemInstruction,
            GeminiGenerationConfig generationConfig
    ) {
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private record GeminiContent(
            String role,
            List<GeminiPart> parts
    ) {
    }

    private record GeminiPart(
            String text
    ) {
    }

    private record GeminiGenerationConfig(
            double temperature,
            int maxOutputTokens
    ) {
    }

    private record GeminiResponse(
            List<GeminiCandidate> candidates
    ) {
    }

    private record GeminiCandidate(
            GeminiContent content
    ) {
    }
}
