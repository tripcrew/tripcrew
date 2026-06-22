package com.tripcrew.user.model;

/**
 * 사용자 권한. DB에는 VARCHAR로 이름(name)이 그대로 저장된다.
 * (MyBatis 기본 EnumTypeHandler 가 enum name <-> VARCHAR 매핑)
 */
public enum Role {
    USER,
    ADMIN,
    /**
     * 최고 책임자. 역할 변경(PATCH /role) 엔드포인트는 SUPER_ADMIN 만 호출할 수 있다.
     * <p>API 로는 절대 부여할 수 없고(엔드포인트는 USER↔ADMIN 토글만 허용),
     * 오직 DB 직접 지정으로만 생성된다 — API 를 통한 최고권한 탈취를 원천 차단하기 위함.
     */
    SUPER_ADMIN
}
