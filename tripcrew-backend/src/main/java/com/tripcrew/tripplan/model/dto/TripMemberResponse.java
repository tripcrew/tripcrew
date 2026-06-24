package com.tripcrew.tripplan.model.dto;

import java.time.LocalDateTime;

import com.tripcrew.tripplan.model.TripMemberRole;
import com.tripcrew.tripplan.model.TripMemberStatus;

/**
 * 멤버 목록 응답. 소유자(OWNER) 행은 가상으로 합성해 함께 내려준다(테이블엔 협업자만 저장되므로).
 * status 로 수락 대기(PENDING) 멤버를 구분해 프론트가 '대기중' 배지를 띄운다.
 */
public record TripMemberResponse(
        Long userId,
        String email,
        String nickname,
        TripMemberRole role,
        TripMemberStatus status,
        LocalDateTime createdAt
) {
    public static TripMemberResponse from(TripMemberRow row) {
        return new TripMemberResponse(
                row.getUserId(),
                row.getEmail(),
                row.getNickname(),
                row.getRole(),
                row.getStatus(),
                row.getCreatedAt());
    }
}
