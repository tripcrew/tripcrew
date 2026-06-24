package com.tripcrew.report.model.dto;

import java.time.LocalDateTime;

/**
 * 관리자 신고 목록 응답(미처리 OPEN 건만 노출). 신고 처리 화면에서 한눈에 판단할 수 있도록
 * 신고일 · 대상종류 · 신고자(이메일) · 사유 · 신고된 후기 내용 · 피신고 유저(이메일)를 내려준다.
 * 처리완료(RESOLVED)/기각(DISMISSED)되면 목록에서 사라진다.
 */
public record AdminReportResponse(
        Long id,
        LocalDateTime createdAt,
        String status,
        LocalDateTime processedAt,
        String targetType,
        String reason,
        String detail,
        String reporterEmail,
        String reviewContent,
        String reportedUserEmail
) {
    public static AdminReportResponse from(AdminReportRow row) {
        return new AdminReportResponse(
                row.getId(),
                row.getCreatedAt(),
                row.getStatus() == null ? null : row.getStatus().name(),
                row.getProcessedAt(),
                row.getTargetType() == null ? null : row.getTargetType().name(),
                row.getReason() == null ? null : row.getReason().name(),
                row.getDetail(),
                row.getReporterEmail(),
                row.getReviewContent(),
                row.getReportedUserEmail()
        );
    }
}
