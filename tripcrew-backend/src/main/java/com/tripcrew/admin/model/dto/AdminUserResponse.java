package com.tripcrew.admin.model.dto;

import java.time.LocalDateTime;

import com.tripcrew.user.model.dto.User;

/**
 * 관리자 화면용 사용자 응답. password 는 절대 포함하지 않는다.
 */
public record AdminUserResponse(
        Long id,
        String email,
        String nickname,
        String role,
        String status,
        LocalDateTime createdAt
) {
    public static AdminUserResponse from(User user) {
        return new AdminUserResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getRole().name(),
                user.getStatus().name(),
                user.getCreatedAt()
        );
    }
}
