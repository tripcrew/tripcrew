package com.tripcrew.review.model;

/**
 * 후기 노출 상태. DB에는 VARCHAR로 이름(name)이 그대로 저장된다(MyBatis 기본 EnumTypeHandler).
 *
 * <p>하드삭제 대신 soft-delete 로 다룬다: 신고 처리완료(RESOLVED) 시 HIDDEN 으로 숨겨
 * 공개 목록에서는 제외하되 row 는 보존한다(증거 보존 + 폴리모픽 dangling 방지).
 */
public enum ReviewStatus {
    /** 공개(기본값) */
    VISIBLE,
    /** 신고 처리로 숨겨짐(공개 목록 제외, row 보존) */
    HIDDEN
}
