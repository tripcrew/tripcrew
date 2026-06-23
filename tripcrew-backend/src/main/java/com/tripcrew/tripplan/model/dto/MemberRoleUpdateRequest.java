package com.tripcrew.tripplan.model.dto;

import com.tripcrew.tripplan.model.TripMemberRole;

import jakarta.validation.constraints.NotNull;

/**
 * 멤버 역할 변경 요청. EDITOR ↔ VIEWER 전환만 의미가 있으며,
 * OWNER 지정은 서비스에서 400 으로 거부(소유권 이전은 범위 밖).
 */
public record MemberRoleUpdateRequest(

        @NotNull
        TripMemberRole role
) {
}
