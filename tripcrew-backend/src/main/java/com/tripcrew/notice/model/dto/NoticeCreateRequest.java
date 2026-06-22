package com.tripcrew.notice.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 공지 작성 요청(관리자). pinned 는 선택(미지정 시 false).
 */
public record NoticeCreateRequest(
        @NotBlank(message = "제목은 필수입니다.")
        @Size(max = 200, message = "제목은 200자 이내여야 합니다.")
        String title,

        @NotBlank(message = "내용은 필수입니다.")
        String content,

        boolean pinned
) {
}
