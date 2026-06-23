package com.tripcrew.coedit.config;

import java.security.Principal;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import com.tripcrew.auth.jwt.JwtProvider;
import com.tripcrew.tripplan.service.TripPlanAccessService;

import lombok.RequiredArgsConstructor;

/**
 * F06 공동편집 — STOMP 인증/인가 인터셉터(들어오는 채널).
 *
 * <ul>
 *   <li><b>CONNECT</b>: {@code Authorization: Bearer} 토큰을 검증(REST 와 동일한 {@link JwtProvider})하고,
 *       userId 를 {@link StompPrincipal} 로 세션에 심는다. 토큰 없거나 무효면 연결 거부.</li>
 *   <li><b>SUBSCRIBE</b>: {@code /topic/plans/{id}/presence} 구독 시 해당 계획의 멤버인지
 *       {@link TripPlanAccessService#requireMember}로 확인(아니면 예외 → 구독 차단).</li>
 * </ul>
 */
@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";
    /** /topic/plans/{planId}/... 에서 planId 추출 */
    private static final Pattern PLAN_DESTINATION = Pattern.compile("/topic/plans/(\\d+)(?:/.*)?");

    private final JwtProvider jwtProvider;
    private final TripPlanAccessService accessService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() == null) {
            return message;
        }

        StompCommand command = accessor.getCommand();
        if (StompCommand.CONNECT.equals(command)) {
            authenticate(accessor);
        } else if (StompCommand.SUBSCRIBE.equals(command)) {
            authorizeSubscription(accessor);
        }
        return message;
    }

    /** CONNECT: Bearer 토큰 검증 후 principal(userId) 주입. */
    private void authenticate(StompHeaderAccessor accessor) {
        String header = accessor.getFirstNativeHeader("Authorization");
        String token = (header != null && header.startsWith(BEARER_PREFIX))
                ? header.substring(BEARER_PREFIX.length())
                : null;
        if (token == null || !jwtProvider.validate(token)) {
            throw new MessagingException("WebSocket 인증에 실패했습니다.");
        }
        accessor.setUser(new StompPrincipal(jwtProvider.getUserId(token)));
    }

    /** SUBSCRIBE: 계획별 토픽이면 멤버 여부 확인(아니면 requireMember 가 예외 → 구독 거부). */
    private void authorizeSubscription(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();
        if (destination == null) {
            return;
        }
        Matcher matcher = PLAN_DESTINATION.matcher(destination);
        if (!matcher.matches()) {
            return; // 계획 토픽이 아니면 통과(현재 다른 토픽 없음)
        }
        Long planId = Long.valueOf(matcher.group(1));
        Long userId = currentUserId(accessor);
        if (userId == null) {
            throw new MessagingException("인증되지 않은 구독입니다.");
        }
        accessService.requireMember(planId, userId); // 멤버 아니면 예외 → 구독 차단
    }

    private Long currentUserId(StompHeaderAccessor accessor) {
        Principal user = accessor.getUser();
        if (user instanceof StompPrincipal sp) {
            return sp.getUserId();
        }
        return null;
    }
}
