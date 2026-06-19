package com.tripcrew.admin.model.dto;

import com.tripcrew.user.model.Role;

import jakarta.validation.constraints.NotNull;

/**
 * 사용자 권한 변경 요청. role 은 USER / ADMIN 중 하나(잘못된 값은 역직렬화에서 400).
 */
public record UpdateRoleRequest(
        @NotNull(message = "role 은 필수입니다.") Role role
) {
}
