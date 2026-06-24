package com.tripcrew.inquiry.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 관리자 답변 요청. 답변 관리자는 인증 주체(JWT)로 정하므로 본문에 받지 않는다.
 */
public record InquiryAnswerRequest(

        @NotBlank
        @Size(max = 5000)
        String answer
) {
}
