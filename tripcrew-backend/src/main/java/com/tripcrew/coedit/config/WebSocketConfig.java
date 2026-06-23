package com.tripcrew.coedit.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

import lombok.RequiredArgsConstructor;

/**
 * F06 공동편집 — STOMP over WebSocket 설정.
 *
 * <p>핸드셰이크 엔드포인트는 {@code /ws}(raw WebSocket). 클라이언트는 STOMP CONNECT 프레임의
 * {@code Authorization: Bearer} 헤더로 인증한다({@link StompAuthChannelInterceptor}).
 * 브로커는 단순 인메모리 브로커({@code /topic} 구독, {@code /app} 으로 들어오는 메시지 처리).
 *
 * <p>P1 은 단일 인스턴스 인메모리 프레즌스. 다중 인스턴스 확장(P3)에서 Redis Pub/Sub 로 브로커를 교체한다.
 */
@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final StompAuthChannelInterceptor authChannelInterceptor;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 핸드셰이크(HTTP 업그레이드)는 SecurityConfig 에서 permitAll, 실제 인증은 STOMP CONNECT 에서.
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("http://localhost:5173", "http://127.0.0.1:5173");
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic");
        registry.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        // 들어오는 모든 STOMP 프레임에 인증/인가 인터셉터 적용(CONNECT 인증, SUBSCRIBE 멤버 확인).
        registration.interceptors(authChannelInterceptor);
    }
}
