package com.tripcrew.admin.model.dto;

import java.util.List;

/**
 * 관리자 대시보드 차트용 통계. 모두 실제 도메인 데이터(users/reviews/reports) 집계라 날조가 없다.
 * 추이(signups/reviews/reports)는 최근 N일을 0 으로 채워 정렬된 상태로 내려간다.
 */
public record AdminDashboardStatsResponse(
        List<DailyCount> signups,
        List<DailyCount> reviews,
        List<DailyCount> reports,
        List<LabelCount> roleDistribution,
        List<LabelCount> statusDistribution
) {
}
