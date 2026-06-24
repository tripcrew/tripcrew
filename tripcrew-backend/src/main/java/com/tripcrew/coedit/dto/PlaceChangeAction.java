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
    REMOVED,
    /** 계획 메타(제목/설명/날짜) 저장 — 수신측은 장소뿐 아니라 계획 정보까지 다시 불러온다. */
    SAVED
}
