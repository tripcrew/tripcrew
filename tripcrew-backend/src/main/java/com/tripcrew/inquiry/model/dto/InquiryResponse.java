package com.tripcrew.inquiry.model.dto;

import java.time.LocalDateTime;

import com.tripcrew.inquiry.model.InquiryStatus;

/**
 * 내 문의 목록 응답 한 건(본인용). 작성자(userId)는 내 문의만 돌려주므로 노출하지 않는다.
 * 답변(answer/answeredAt)은 ANSWERED 일 때만 채워진다.
 */
public record InquiryResponse(
        Long id,
        String title,
        String content,
        InquiryStatus status,
        String answer,
        LocalDateTime answeredAt,
        LocalDateTime createdAt
) {
    public static InquiryResponse from(Inquiry i) {
        return new InquiryResponse(
                i.getId(), i.getTitle(), i.getContent(), i.getStatus(),
                i.getAnswer(), i.getAnsweredAt(), i.getCreatedAt());
    }
}
