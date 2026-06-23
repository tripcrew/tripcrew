package com.tripcrew.coedit.config;

import java.security.Principal;

/**
 * STOMP 세션의 인증 주체. 이름(name)에 userId 문자열을 담아 메시지 핸들러에서
 * {@code Long.valueOf(principal.getName())} 으로 사용자 식별에 쓴다.
 */
public class StompPrincipal implements Principal {

    private final Long userId;

    public StompPrincipal(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    @Override
    public String getName() {
        return String.valueOf(userId);
    }
}
