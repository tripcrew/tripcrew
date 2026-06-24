package com.tripcrew.inquiry.model.dto;

import java.time.LocalDateTime;

import com.tripcrew.inquiry.model.InquiryStatus;

import lombok.Getter;
import lombok.Setter;

/**
 * 관리자 문의 목록 조회용 조인 프로젝션(읽기 전용). MyBatis 가 setter 로 채운다.
 * 작성자는 식별이 명확한 이메일로 내려준다(PK 노출 대신, 신고 목록과 동일한 방식).
 */
@Getter
@Setter
public class AdminInquiryRow {

    private Long id;
    private String userEmail;       // 문의 작성자
    private String title;
    private String content;
    private InquiryStatus status;
    private String answer;
    private LocalDateTime answeredAt;
    private LocalDateTime createdAt;
}
