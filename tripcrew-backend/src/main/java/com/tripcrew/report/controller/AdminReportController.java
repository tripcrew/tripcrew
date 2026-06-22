package com.tripcrew.report.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.tripcrew.report.model.ReportStatus;
import com.tripcrew.report.model.dto.AdminReportResponse;
import com.tripcrew.report.service.AdminReportService;

import lombok.RequiredArgsConstructor;

/**
 * F09 Phase 2: 관리자 신고 목록/처리. /api/admin/** 은 SecurityConfig 에서 ROLE_ADMIN 전용.
 * 처리완료(resolve)는 누적 자동 제재(3회)와 연동되고, 기각(dismiss)은 카운트 없이 종료한다.
 */
@RestController
@RequestMapping("/api/admin/reports")
@RequiredArgsConstructor
public class AdminReportController {

    private final AdminReportService adminReportService;

    /** 신고 목록. status 미지정이면 전체, 지정하면 해당 상태만(보통 ?status=OPEN). */
    @GetMapping
    public List<AdminReportResponse> list(@RequestParam(required = false) ReportStatus status) {
        return adminReportService.list(status);
    }

    /** 처리완료: 피신고 유저 누적 +1, 3회 이상이면 자동 제재(일반 USER). */
    @PatchMapping("/{id}/resolve")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resolve(@PathVariable Long id) {
        adminReportService.resolve(id);
    }

    /** 기각: 신고 사유가 부적절한 경우. 누적 카운트 없이 종료. */
    @PatchMapping("/{id}/dismiss")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void dismiss(@PathVariable Long id) {
        adminReportService.dismiss(id);
    }
}
