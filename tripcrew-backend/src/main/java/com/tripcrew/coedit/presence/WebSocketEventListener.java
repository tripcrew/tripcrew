package com.tripcrew.coedit.presence;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import lombok.RequiredArgsConstructor;

/**
 * STOMP 세션 종료 시 프레즌스에서 제거. 탭 닫힘/네트워크 끊김/로그아웃 모두 여기로 들어온다.
 */
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private final PresenceService presenceService;

    @EventListener
    public void onDisconnect(SessionDisconnectEvent event) {
        presenceService.disconnect(event.getSessionId());
    }
}
