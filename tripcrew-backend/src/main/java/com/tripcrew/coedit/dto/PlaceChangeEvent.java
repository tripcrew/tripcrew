package com.tripcrew.coedit.dto;

/**
 * F06 P2a — 장소 변경 브로드캐스트 메시지({@code /topic/plans/{id}/places}).
 *
 * <p>누가({@code actorId}/{@code actorNickname}) 어떤 동작({@code action})을 했는지 담는다.
 * 수신측은 자기 자신이 보낸 변경이면 무시하고, 그 외에는 장소 목록을 다시 조회한 뒤
 * "○○님이 …" 토스트를 띄운다. 닉네임은 서버가 신뢰 출처(users)에서 해석해 내려준다.
 *
 * <p>{@code targetUserId} 는 특정 사용자를 겨냥한 동작(예: MEMBER_REMOVED 내보내기)에서만 채워지며,
 * 그 외 장소 동작에서는 {@code null} 이다. 수신측은 본인이 대상일 때만 반응한다.
 */
public record PlaceChangeEvent(Long actorId, String actorNickname, String action, Long targetUserId) {
}
