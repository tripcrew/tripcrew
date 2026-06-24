package com.tripcrew.notification.model;

/**
 * 회원 알림 유형. DB에는 VARCHAR로 이름(name)이 그대로 저장된다(MyBatis 기본 EnumTypeHandler).
 *
 * <p>범용 알림 인프라이므로 트리거가 늘면 값을 추가한다(컬럼 COMMENT 만 갱신 — ENUM 금지 규칙).
 * 프론트는 {@code type} 으로 알림 클릭 시 이동 경로를 분기한다.
 */
public enum NotificationType {
    /** 내가 한 신고가 검토되어 조치 완료됨(ref_id = 신고 id) */
    REPORT_RESOLVED,
    /** 내가 한 신고가 검토 결과 기각됨(ref_id = 신고 id) */
    REPORT_DISMISSED,
    /** 여행 종료 후 후기 작성 격려(ref_id = 여행계획 id) */
    REVIEW_NUDGE,
    /** 여행계획 공동편집 초대(ref_id = 여행계획 id) */
    INVITE,
    /** 내 1:1 문의에 관리자 답변이 등록됨(ref_id = 문의 id) */
    INQUIRY_ANSWERED,
    /** 신고 누적으로 내 계정에 단계 제재가 적용됨(후기/계획 작성 금지·계정 임시정지). ref_id 없음 */
    SANCTION_APPLIED,
    /** (관리자 수신) 신고 누적이 최고 임계에 도달 — 영구정지 검토 필요(ref_id = 대상 사용자 id) */
    SANCTION_REVIEW_REQUIRED
}
