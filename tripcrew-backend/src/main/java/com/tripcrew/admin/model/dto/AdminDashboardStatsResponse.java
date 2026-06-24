package com.tripcrew.admin.model.dto;

import java.util.List;

/**
 * 관리자 대시보드 차트용 통계. 모두 실제 도메인 데이터(users/reviews/reports) 집계라 날조가 없다.
 * 활동 추이(reviews/reports)는 최근 N일을 0 으로 채워 정렬된 상태로 내려간다.
 * (가입 추이는 연/월 선택형이라 별도 엔드포인트 {@code /signups} 로 분리)
 */
public record AdminDashboardStatsResponse(
        List<DailyCount> reviews,
        List<DailyCount> reports,
        List<LabelCount> roleDistribution,
        List<LabelCount> statusDistribution
) {
}
