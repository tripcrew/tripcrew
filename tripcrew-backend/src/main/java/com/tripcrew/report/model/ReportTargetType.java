package com.tripcrew.report.model;

/**
 * 신고 대상 종류(폴리모픽). DB에는 VARCHAR로 이름(name)이 그대로 저장된다.
 * target_id 가 가리키는 테이블이 종류별로 달라 DB FK 를 둘 수 없으므로
 * 대상 존재 검증은 앱레벨(ReportService)에서 한다.
 */
public enum ReportTargetType {
    /** 후기(reviews.id) */
    REVIEW,
    /** 사용자(users.id) */
    USER
}
