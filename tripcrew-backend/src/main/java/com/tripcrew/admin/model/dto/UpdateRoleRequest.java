package com.tripcrew.admin.model.dto;

import com.tripcrew.user.model.Role;

import jakarta.validation.constraints.NotNull;

/**
 * 사용자 권한 변경 요청. 알 수 없는 값은 역직렬화에서 400.
 * <p>엔드포인트로 허용되는 값은 USER / ADMIN 뿐 — SUPER_ADMIN 은 enum 으로는 받지만
 * 서비스에서 거부한다(최고 권한은 DB 직접 지정만, API 부여 불가).
 */
public record UpdateRoleRequest(
        @NotNull(message = "role 은 필수입니다.") Role role
) {
}
