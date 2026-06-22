package com.tripcrew.user.model;

/**
 * 사용자 계정 상태. DB에는 VARCHAR로 이름(name)이 그대로 저장된다.
 * (MyBatis 기본 EnumTypeHandler 가 enum name <-> VARCHAR 매핑)
 */
public enum Status {
    /** 정상. 로그인/이용 가능. */
    ACTIVE,
    /**
     * 제재(밴)된 계정. 로그인·토큰 재발급이 차단된다.
     * <p>밴은 관리자(ADMIN/SUPER_ADMIN)가 부여하며, SUPER_ADMIN 대상은 밴할 수 없다.
     */
    BANNED
}
