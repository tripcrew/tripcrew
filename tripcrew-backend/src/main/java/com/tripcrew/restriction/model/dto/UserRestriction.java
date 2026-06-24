package com.tripcrew.restriction.model.dto;

import java.time.LocalDateTime;

import com.tripcrew.restriction.model.RestrictionType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * user_restrictions 테이블 매핑. (MyBatis 가 setter 로 채우므로 기본생성자/세터 필요)
 * created_at 은 DB DEFAULT 로 채워지므로 INSERT 시 다루지 않는다.
 *
 * <p>{@code until} 이 NULL 이거나 현재보다 미래면 "활성" 제재다(과거면 만료 — 무시).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRestriction {

    private Long id;
    private Long userId;
    private RestrictionType type;
    private LocalDateTime until;   // NULL = 영구
    private String reason;
    private LocalDateTime createdAt;
}
