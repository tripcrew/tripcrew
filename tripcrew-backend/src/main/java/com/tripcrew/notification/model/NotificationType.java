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
    INQUIRY_ANSWERED
}
