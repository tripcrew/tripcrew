package com.tripcrew.tripplan.model.dto;

import com.tripcrew.tripplan.model.TripMemberRole;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 멤버 초대 요청. 이메일로 기존 가입 사용자를 찾아 협업자로 추가한다.
 * role 은 EDITOR 또는 VIEWER 만 허용(OWNER 지정은 서비스에서 400 으로 거부 — 소유권 이전은 범위 밖).
 */
public record MemberInviteRequest(

        @NotBlank @Email
        String email,

        @NotNull
        TripMemberRole role
) {
}
