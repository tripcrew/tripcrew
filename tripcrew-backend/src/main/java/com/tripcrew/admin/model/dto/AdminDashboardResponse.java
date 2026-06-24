package com.tripcrew.admin.model.dto;

/**
 * 관리자 대시보드 집계 응답. 모두 COUNT(*) 기반이라 별도 테이블/마이그레이션 없이 산출한다.
 *
 * <p>{@code chatbotUsageCount}/{@code qnaCount} 는 아직 출처가 없어 null 로 내려간다(프론트는 "준비 중" 표시):
 * <ul>
 *   <li>챗봇 사용현황 — 트랙 B(F05) 데이터라 출처 협의 후 연동.</li>
 *   <li>Q&A — 1:1 문의 기능(②) 구현 후 연동.</li>
 * </ul>
 */
public record AdminDashboardResponse(
        long userCount,
        long bannedUserCount,
        long openReportCount,
        long noticeCount,
        Long chatbotUsageCount,
        Long qnaCount
) {
}
