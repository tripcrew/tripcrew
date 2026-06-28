package com.tripcrew.user.model;

/**
 * 가입/로그인 경로. DB에는 VARCHAR로 이름(name)이 그대로 저장된다.
 * (MyBatis 기본 EnumTypeHandler 가 enum name <-> VARCHAR 매핑)
 */
public enum Provider {
    /** 이메일+비밀번호 자체 가입. password 보유, provider_id 는 NULL. */
    LOCAL,
    /** 카카오 소셜 로그인. password 는 NULL, provider_id = 카카오 회원번호. */
    KAKAO,
    /** 네이버 소셜 로그인. password 는 NULL, provider_id = 네이버 식별자. */
    NAVER
}
