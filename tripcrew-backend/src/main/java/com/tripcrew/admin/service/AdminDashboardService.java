package com.tripcrew.admin.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tripcrew.admin.model.dto.AdminDashboardResponse;
import com.tripcrew.notice.model.mapper.NoticeMapper;
import com.tripcrew.report.model.ReportStatus;
import com.tripcrew.report.model.mapper.ReportMapper;
import com.tripcrew.user.model.Status;
import com.tripcrew.user.model.mapper.UserMapper;

import lombok.RequiredArgsConstructor;

/**
 * F09 관리자 - 대시보드 집계. 모든 진입은 ROLE_ADMIN 으로 SecurityConfig 에서 막혀 있다.
 * 단순 COUNT(*) 집계라 마이그레이션 없이 산출한다.
 */
@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final UserMapper userMapper;
    private final ReportMapper reportMapper;
    private final NoticeMapper noticeMapper;

    @Transactional(readOnly = true)
    public AdminDashboardResponse summary() {
        long userCount = userMapper.countAll();
        long bannedUserCount = userMapper.countByStatus(Status.BANNED);
        long openReportCount = reportMapper.countByStatus(ReportStatus.OPEN);
        long noticeCount = noticeMapper.countAll();
        // 챗봇 사용현황(트랙 B)·Q&A(1:1 문의 ②) 는 출처가 생기면 연동 — 그전까지 null("준비 중").
        return new AdminDashboardResponse(userCount, bannedUserCount, openReportCount, noticeCount, null, null);
    }
}
