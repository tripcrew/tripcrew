package com.tripcrew.inquiry.model.dto;

import java.time.LocalDateTime;

/**
 * 관리자 문의 목록 응답. 답변 화면에서 한눈에 판단할 수 있도록
 * 작성일 · 작성자(이메일) · 제목 · 내용 · 상태 · 기존 답변(있으면)을 내려준다.
 * status 로 미답변(OPEN)/답변완료(ANSWERED)를 구분한다.
 */
public record AdminInquiryResponse(
        Long id,
        String userEmail,
        String title,
        String content,
        String status,
        String answer,
        LocalDateTime answeredAt,
        LocalDateTime createdAt
) {
    public static AdminInquiryResponse from(AdminInquiryRow row) {
        return new AdminInquiryResponse(
                row.getId(),
                row.getUserEmail(),
                row.getTitle(),
                row.getContent(),
                row.getStatus() == null ? null : row.getStatus().name(),
                row.getAnswer(),
                row.getAnsweredAt(),
                row.getCreatedAt());
    }
}
