package com.tripcrew.coedit.edit;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.tripcrew.coedit.dto.PlaceChangeAction;
import com.tripcrew.coedit.dto.PlaceChangeEvent;
import com.tripcrew.user.model.dto.User;
import com.tripcrew.user.model.mapper.UserMapper;

import lombok.RequiredArgsConstructor;

/**
 * F06 P2a — 장소 변경을 같은 계획을 편집 중인 다른 접속자에게 알린다.
 *
 * <p>엔티티 단위 broadcast-refetch: 변경 본문이 아니라 (행위자, 동작)만 {@code /topic/plans/{id}/places}
 * 로 보내고, 수신측이 장소 목록을 다시 조회한다. 충돌 처리(409)·op 적용 고도화는 P2b 이후.
 *
 * <p>컨트롤러에서 변경 트랜잭션이 커밋된 뒤 호출해, 수신측 refetch 가 항상 반영된 상태를 읽도록 한다.
 * 단일 인스턴스 인메모리 브로커 기준이며, 다중 인스턴스(P3)에서는 브로커를 Redis Pub/Sub 로 교체한다.
 */
@Service
@RequiredArgsConstructor
public class PlaceChangeBroadcaster {

    private final SimpMessagingTemplate messagingTemplate;
    private final UserMapper userMapper;

    public void broadcast(Long planId, Long actorId, PlaceChangeAction action) {
        String nickname = userMapper.findById(actorId)
                .map(User::getNickname)
                .orElse("사용자");
        messagingTemplate.convertAndSend(
                "/topic/plans/" + planId + "/places",
                new PlaceChangeEvent(actorId, nickname, action.name()));
    }
}
