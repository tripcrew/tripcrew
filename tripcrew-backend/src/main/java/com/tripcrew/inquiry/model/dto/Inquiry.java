package com.tripcrew.inquiry.model.dto;

import java.time.LocalDateTime;

import com.tripcrew.inquiry.model.InquiryStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * inquiries 테이블 매핑. (MyBatis 가 setter 로 채우므로 기본생성자/세터 필요)
 *
 * <p>{@code status}(DEFAULT 'OPEN')·{@code created_at}·{@code updated_at} 은 DB DEFAULT 로
 * 채워지므로 INSERT 시 다루지 않는다. 답변(answer/answeredBy/answeredAt)은 답변 처리 때 채워진다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inquiry {

    private Long id;
    private Long userId;
    private String title;
    private String content;
    private InquiryStatus status;
    private String answer;
    private Long answeredBy;
    private LocalDateTime answeredAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
