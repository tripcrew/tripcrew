package com.tripcrew.report.model;

/**
 * 신고 사유 코드. DB에는 VARCHAR로 이름(name)이 그대로 저장된다.
 * 표시 문구(라벨)는 프론트가 코드별로 매핑한다.
 */
public enum ReportReason {
    /** 스팸/도배 */
    SPAM,
    /** 욕설/비방 */
    ABUSE,
    /** 광고/홍보 */
    ADVERTISING,
    /** 부적절한 내용 */
    INAPPROPRIATE,
    /** 기타(detail 참고) */
    OTHER
}
