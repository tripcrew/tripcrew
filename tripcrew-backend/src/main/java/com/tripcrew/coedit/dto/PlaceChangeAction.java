package com.tripcrew.coedit.dto;

/**
 * F06 P2a — 공동편집 중 일어난 장소 변경의 종류.
 *
 * <p>엔티티 단위 broadcast-refetch 방식이라 변경 "내용"이 아니라 "무슨 동작이 일어났는지"만 알린다.
 * 수신측은 이 값으로 토스트 문구를 정하고 실제 목록은 다시 조회한다.
 */
public enum PlaceChangeAction {
    ADDED,
    SCHEDULED,
    REORDERED,
    OPTIMIZED,
    REMOVED
}
