package com.tripcrew.auth.oauth;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

/**
 * 소셜 로그인 성공 → 프론트 콜백 사이의 <b>일회용 코드</b> 저장소(Redis).
 *
 * <p>토큰을 URL 에 직접 싣지 않기 위함: 성공 핸들러가 단기 코드를 발급해 프론트로 리다이렉트하고,
 * 프론트는 그 코드를 {@code POST /api/auth/oauth/exchange} 로 보내 진짜 JWT 로 교환한다.
 * 코드는 짧은 TTL + 1회 사용(getAndDelete) 으로 재사용을 막는다.
 */
@Component
@RequiredArgsConstructor
public class OAuthCodeStore {

    private static final String KEY_PREFIX = "oauth:code:";
    private static final Duration TTL = Duration.ofSeconds(60);

    private final StringRedisTemplate redisTemplate;

    /** userId 에 대한 일회용 코드 발급. */
    public String issue(Long userId) {
        String code = UUID.randomUUID().toString().replace("-", "");
        redisTemplate.opsForValue().set(KEY_PREFIX + code, userId.toString(), TTL);
        return code;
    }

    /** 코드 소비(1회). 유효하면 userId 반환하고 즉시 삭제, 아니면 empty. */
    public Optional<Long> consume(String code) {
        if (code == null || code.isBlank()) {
            return Optional.empty();
        }
        String userId = redisTemplate.opsForValue().getAndDelete(KEY_PREFIX + code);
        return userId == null ? Optional.empty() : Optional.of(Long.valueOf(userId));
    }
}
