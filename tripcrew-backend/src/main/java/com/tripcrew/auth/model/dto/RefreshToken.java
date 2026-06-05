package com.tripcrew.auth.model.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * refresh_tokens 테이블 매핑. (Redis 아닌 DB 저장 방식 - 단순화)
 * 사용자당 1개 정책: 재발급/로그인 시 기존 토큰 삭제 후 새로 저장.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    private Long id;
    private Long userId;
    private String token;
    private LocalDateTime expiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
