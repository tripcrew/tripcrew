package com.tripcrew.tripplan.model.dto;

import java.time.LocalDateTime;

import com.tripcrew.tripplan.model.TripMemberRole;

/**
 * 멤버 목록 응답. 소유자(OWNER) 행은 가상으로 합성해 함께 내려준다(테이블엔 협업자만 저장되므로).
 */
public record TripMemberResponse(
        Long userId,
        String email,
        String nickname,
        TripMemberRole role,
        LocalDateTime createdAt
) {
    public static TripMemberResponse from(TripMemberRow row) {
        return new TripMemberResponse(
                row.getUserId(),
                row.getEmail(),
                row.getNickname(),
                row.getRole(),
                row.getCreatedAt());
    }
}
