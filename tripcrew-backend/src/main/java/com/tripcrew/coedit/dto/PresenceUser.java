package com.tripcrew.coedit.dto;

/**
 * 프레즌스 roster 한 명. 같은 계획을 보고 있는 접속자 표시용(userId + 닉네임).
 * 닉네임은 서버가 신뢰 가능한 출처(users)에서 해석해 내려준다.
 */
public record PresenceUser(Long userId, String nickname) {
}
