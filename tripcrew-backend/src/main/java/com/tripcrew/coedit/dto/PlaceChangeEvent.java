package com.tripcrew.coedit.dto;

/**
 * F06 P2a — 장소 변경 브로드캐스트 메시지({@code /topic/plans/{id}/places}).
 *
 * <p>누가({@code actorId}/{@code actorNickname}) 어떤 동작({@code action})을 했는지만 담는다.
 * 수신측은 자기 자신이 보낸 변경이면 무시하고, 그 외에는 장소 목록을 다시 조회한 뒤
 * "○○님이 …" 토스트를 띄운다. 닉네임은 서버가 신뢰 출처(users)에서 해석해 내려준다.
 */
public record PlaceChangeEvent(Long actorId, String actorNickname, String action) {
}
