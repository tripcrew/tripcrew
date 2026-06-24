package com.tripcrew.inquiry.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 1:1 문의 작성 요청. 작성자는 인증 주체(JWT)로 정하므로 본문에 받지 않는다.
 */
public record InquiryCreateRequest(

        @NotBlank
        @Size(max = 150)
        String title,

        @NotBlank
        @Size(max = 5000)
        String content
) {
}
