package com.tripcrew.user.model;

/**
 * 사용자 권한. DB에는 VARCHAR로 이름(name)이 그대로 저장된다.
 * (MyBatis 기본 EnumTypeHandler 가 enum name <-> VARCHAR 매핑)
 */
public enum Role {
    USER,
    ADMIN
}
