package com.tripcrew.admin.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tripcrew.admin.model.dto.AdminDashboardResponse;
import com.tripcrew.admin.service.AdminDashboardService;

import lombok.RequiredArgsConstructor;

/**
 * F09 관리자 - 대시보드 집계 카운트. /api/admin/** 은 SecurityConfig 에서 ROLE_ADMIN 전용
 * (별도 인가 규칙·SecurityConfig 변경 불필요).
 */
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping
    public AdminDashboardResponse summary() {
        return adminDashboardService.summary();
    }
}
