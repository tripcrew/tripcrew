package com.tripcrew.admin.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tripcrew.admin.model.dto.AdminDashboardResponse;
import com.tripcrew.admin.model.dto.AdminDashboardStatsResponse;
import com.tripcrew.admin.model.dto.DailyCount;
import com.tripcrew.admin.model.dto.ExternalApiHealthResponse;
import com.tripcrew.admin.service.AdminDashboardService;
import com.tripcrew.admin.service.AdminExternalHealthService;

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
    private final AdminExternalHealthService adminExternalHealthService;

    @GetMapping
    public AdminDashboardResponse summary() {
        return adminDashboardService.summary();
    }

    /** 차트용 통계(최근 14일 활동 추이 + 역할/상태 분포). */
    @GetMapping("/stats")
    public AdminDashboardStatsResponse stats() {
        return adminDashboardService.stats();
    }

    /** 가입 추이(선택 연/월의 일자별, 1일~말일). 파라미터 생략 시 이번 달. */
    @GetMapping("/signups")
    public List<DailyCount> signups(@RequestParam(required = false) Integer year,
                                    @RequestParam(required = false) Integer month) {
        return adminDashboardService.signupsForMonth(year, month);
    }

    /** 외부 API 상태. 관리자 대시보드 표시용이며 API 키 값은 절대 내려주지 않는다. */
    @GetMapping("/external-health")
    public ExternalApiHealthResponse externalHealth(@RequestParam(defaultValue = "false") boolean live) {
        return adminExternalHealthService.externalHealth(live);
    }
}
