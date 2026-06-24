package com.tripcrew.restriction.model;

/**
 * 사용자 단계별 제재 종류. DB에는 VARCHAR로 이름(name)이 그대로 저장된다
 * (MyBatis 기본 EnumTypeHandler 가 enum name &lt;-&gt; VARCHAR 매핑).
 *
 * <p>신고 누적 임계에 따라 약→강으로 적용된다(로드맵 ④ graduated sanctions).
 * 영구정지(최고 단계)는 별도 type 이 아니라 기존 {@code users.status='BANNED'} 를 재사용하며,
 * 자동이 아닌 관리자 수동 확정으로만 적용한다.
 */
public enum RestrictionType {
    /** 후기 작성 금지(POST /api/reviews 차단). 브라우징·다른 기능은 가능. */
    REVIEW_WRITE,
    /** 여행계획 작성 금지(POST /api/trip-plans 차단). */
    PLAN_CREATE,
    /** 계정 임시 정지. login/reissue 차단(기발급 access token 은 만료 전까지 유효). */
    ACCOUNT_SUSPEND
}
