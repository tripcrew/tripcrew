package com.tripcrew.review.model.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 후기 수정 요청. 대상(targetType/targetId)은 바꿀 수 없으므로 받지 않는다(평점/내용만 수정).
 * 본인 여부는 서비스에서 검증한다.
 */
public record ReviewUpdateRequest(

        @NotNull @Min(1) @Max(5)
        Integer rating,

        @Size(max = 1000)
        String content
) {
}
