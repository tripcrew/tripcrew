package com.tripcrew.admin.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.tripcrew.admin.model.dto.AdminUserResponse;
import com.tripcrew.admin.model.dto.UpdateRoleRequest;
import com.tripcrew.admin.service.AdminUserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * F09 관리자 - 사용자 관리. /api/admin/** 은 SecurityConfig 에서 ROLE_ADMIN 전용.
 */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public List<AdminUserResponse> list() {
        return adminUserService.listUsers();
    }

    @PatchMapping("/{id}/role")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateRole(@AuthenticationPrincipal Long requesterId,
                           @PathVariable Long id,
                           @Valid @RequestBody UpdateRoleRequest request) {
        adminUserService.updateRole(requesterId, id, request.role());
    }
}
