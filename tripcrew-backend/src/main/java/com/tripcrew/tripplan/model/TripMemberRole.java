package com.tripcrew.tripplan.model;

/**
 * 여행계획 참여자 역할(F06 공동편집). trip_members.role 에 VARCHAR 로 저장된다.
 *
 * <ul>
 *   <li>OWNER  - 계획 소유자. trip_plans.owner_id 가 단일 진실이며 trip_members 엔 보통 저장하지 않는다.
 *               멤버 관리(초대/역할변경/제거)와 계획 삭제 권한을 가진다.</li>
 *   <li>EDITOR - 계획 메타와 장소(추가/삭제/순서/Day/메모)를 편집할 수 있다.</li>
 *   <li>VIEWER - 읽기 전용.</li>
 * </ul>
 */
public enum TripMemberRole {
    OWNER,
    EDITOR,
    VIEWER;

    /** 편집 권한(계획 메타·장소 변경) 보유 여부. */
    public boolean canEdit() {
        return this == OWNER || this == EDITOR;
    }
}
