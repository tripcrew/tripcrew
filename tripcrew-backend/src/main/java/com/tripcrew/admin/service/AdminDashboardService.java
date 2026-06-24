package com.tripcrew.admin.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tripcrew.admin.model.dto.AdminDashboardResponse;
import com.tripcrew.admin.model.dto.AdminDashboardStatsResponse;
import com.tripcrew.admin.model.dto.DailyCount;
import com.tripcrew.admin.model.dto.LabelCount;
import com.tripcrew.admin.model.mapper.AdminStatsMapper;
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

    /** 추이 차트 기간(일). 오늘 포함 최근 14일. */
    private static final int TREND_DAYS = 14;

    private final UserMapper userMapper;
    private final ReportMapper reportMapper;
    private final NoticeMapper noticeMapper;
    private final AdminStatsMapper adminStatsMapper;

    @Transactional(readOnly = true)
    public AdminDashboardResponse summary() {
        long userCount = userMapper.countAll();
        long bannedUserCount = userMapper.countByStatus(Status.BANNED);
        long openReportCount = reportMapper.countByStatus(ReportStatus.OPEN);
        long noticeCount = noticeMapper.countAll();
        // 챗봇 사용현황(트랙 B)·Q&A(1:1 문의 ②) 는 출처가 생기면 연동 — 그전까지 null("준비 중").
        return new AdminDashboardResponse(userCount, bannedUserCount, openReportCount, noticeCount, null, null);
    }

    /** 차트용 통계: 최근 14일 가입/후기/신고 추이(0 채움) + 역할/상태 분포. */
    @Transactional(readOnly = true)
    public AdminDashboardStatsResponse stats() {
        // 오늘 포함 최근 14일의 0시를 기준으로 조회(경계 누락 방지).
        LocalDate startDate = LocalDate.now().minusDays(TREND_DAYS - 1L);
        LocalDateTime since = startDate.atStartOfDay();
        return new AdminDashboardStatsResponse(
                fillDays(adminStatsMapper.signupsByDay(since), startDate),
                fillDays(adminStatsMapper.reviewsByDay(since), startDate),
                fillDays(adminStatsMapper.reportsByDay(since), startDate),
                adminStatsMapper.roleDistribution(),
                adminStatsMapper.statusDistribution());
    }

    /**
     * 희소한 일자별 카운트를 startDate 부터 TREND_DAYS 일까지 0 으로 채워 연속된 시계열로 만든다.
     * (DB 에 없는 날짜는 0 — 차트가 끊기지 않도록)
     */
    private List<DailyCount> fillDays(List<DailyCount> rows, LocalDate startDate) {
        Map<LocalDate, Long> byDay = new LinkedHashMap<>();
        for (DailyCount row : rows) {
            byDay.put(row.getDay(), row.getCount());
        }
        List<DailyCount> filled = new ArrayList<>(TREND_DAYS);
        for (int i = 0; i < TREND_DAYS; i++) {
            LocalDate day = startDate.plusDays(i);
            DailyCount dc = new DailyCount();
            dc.setDay(day);
            dc.setCount(byDay.getOrDefault(day, 0L));
            filled.add(dc);
        }
        return filled;
    }
}
