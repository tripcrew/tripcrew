package com.tripcrew.report.model;

/**
 * 신고 처리 상태. DB에는 VARCHAR로 이름(name)이 그대로 저장되며 DB DEFAULT 는 'OPEN'.
 */
public enum ReportStatus {
    /** 접수됨(미처리). 관리자 검토 대기. */
    OPEN,
    /** 처리 완료(제재 등 조치함). */
    RESOLVED,
    /** 기각(문제 없음). */
    DISMISSED
}
