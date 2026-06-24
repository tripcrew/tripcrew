package com.tripcrew.admin.model.dto;

/**
 * 관리자 대시보드 집계 응답. 모두 COUNT(*) 기반이라 별도 테이블/마이그레이션 없이 산출한다.
 *
 * <ul>
 *   <li>{@code chatbotUsageCount} — 챗봇 누적 사용자 턴(chat_messages, F05).</li>
 *   <li>{@code qnaCount} — 미답변(OPEN) 1:1 문의 건수(inquiries, 로드맵 ③).</li>
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
