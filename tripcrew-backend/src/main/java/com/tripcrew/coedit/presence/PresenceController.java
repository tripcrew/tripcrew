package com.tripcrew.coedit.presence;

import java.security.Principal;

import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

import lombok.RequiredArgsConstructor;

/**
 * F06 P1 — 프레즌스 입장 처리.
 *
 * <p>클라이언트가 {@code /topic/plans/{id}/presence} 를 구독한 뒤(인가는 인터셉터에서)
 * {@code /app/plans/{id}/join} 으로 입장 신호를 보내면 roster 에 추가하고 브로드캐스트한다.
 * 퇴장은 STOMP 연결 종료 이벤트({@code WebSocketEventListener})에서 자동 처리.
 */
@Controller
@RequiredArgsConstructor
public class PresenceController {

    private final PresenceService presenceService;

    @MessageMapping("/plans/{planId}/join")
    public void join(@DestinationVariable Long planId,
                     Principal principal,
                     SimpMessageHeaderAccessor headerAccessor) {
        Long userId = Long.valueOf(principal.getName());
        presenceService.join(planId, headerAccessor.getSessionId(), userId);
    }
}
