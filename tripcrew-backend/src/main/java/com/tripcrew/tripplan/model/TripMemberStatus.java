package com.tripcrew.tripplan.model;

/**
 * 여행계획 멤버 초대 상태(F06 공동편집 P4). trip_members.status 에 VARCHAR 로 저장된다.
 *
 * <ul>
 *   <li>PENDING  - 초대받아 수락을 기다리는 상태. 권한 판정·공유 목록에서 제외(접근 불가).</li>
 *   <li>ACCEPTED - 초대를 수락해 정식으로 참여한 상태.</li>
 * </ul>
 *
 * <p>거절은 상태가 아니라 행 삭제로 처리하므로 REJECTED 값은 두지 않는다(재초대 가능).
 */
public enum TripMemberStatus {
    PENDING,
    ACCEPTED
}
