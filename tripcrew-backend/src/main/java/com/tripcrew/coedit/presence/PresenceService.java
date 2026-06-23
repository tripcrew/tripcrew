package com.tripcrew.coedit.presence;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.tripcrew.coedit.dto.PresenceUser;
import com.tripcrew.user.model.dto.User;
import com.tripcrew.user.model.mapper.UserMapper;

import lombok.RequiredArgsConstructor;

/**
 * F06 P1 — 계획별 실시간 접속자(프레즌스) 추적.
 *
 * <p>단일 인스턴스 인메모리 구현. STOMP 세션 단위로 (계획, 사용자, 닉네임)을 기억하고,
 * join/disconnect 시 해당 계획 토픽으로 현재 roster 를 브로드캐스트한다.
 * 같은 사용자가 여러 탭(세션)으로 들어와도 roster 에는 1명으로 합쳐 보여준다.
 *
 * <p>다중 인스턴스 확장(P3)에서는 이 인메모리 맵을 Redis(Pub/Sub + 공유 상태)로 교체한다.
 */
@Service
@RequiredArgsConstructor
public class PresenceService {

    private record Session(Long planId, Long userId, String nickname) {
    }

    /** sessionId → 세션 정보. 동시 접근 대비 ConcurrentHashMap. */
    private final Map<String, Session> sessions = new ConcurrentHashMap<>();

    private final SimpMessagingTemplate messagingTemplate;
    private final UserMapper userMapper;

    /** 사용자가 계획 편집 화면에 입장. 닉네임은 신뢰 출처(users)에서 해석해 저장. */
    public void join(Long planId, String sessionId, Long userId) {
        String nickname = userMapper.findById(userId)
                .map(User::getNickname)
                .orElse("사용자");
        sessions.put(sessionId, new Session(planId, userId, nickname));
        broadcastRoster(planId);
    }

    /** STOMP 세션 종료(연결 끊김/탭 닫힘). 해당 세션을 제거하고 그 계획 roster 갱신. */
    public void disconnect(String sessionId) {
        Session removed = sessions.remove(sessionId);
        if (removed != null) {
            broadcastRoster(removed.planId());
        }
    }

    /** 해당 계획의 현재 접속자 목록을 /topic/plans/{planId}/presence 로 전송(userId 기준 중복 제거). */
    private void broadcastRoster(Long planId) {
        Map<Long, String> distinctByUser = new LinkedHashMap<>();
        for (Session session : sessions.values()) {
            if (session.planId().equals(planId)) {
                distinctByUser.putIfAbsent(session.userId(), session.nickname());
            }
        }
        List<PresenceUser> roster = distinctByUser.entrySet().stream()
                .map(entry -> new PresenceUser(entry.getKey(), entry.getValue()))
                .toList();
        messagingTemplate.convertAndSend("/topic/plans/" + planId + "/presence", roster);
    }
}
