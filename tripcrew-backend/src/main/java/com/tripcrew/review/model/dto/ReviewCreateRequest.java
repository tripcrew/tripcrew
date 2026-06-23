package com.tripcrew.review.model.dto;

import com.tripcrew.review.model.ReviewTargetType;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 후기 작성 요청. 작성자는 인증 주체(JWT)로 정하므로 본문에 받지 않는다.
 * 대상은 폴리모픽이며(targetType + targetId) 존재 검증은 서비스에서 한다.
 */
public record ReviewCreateRequest(

        @NotNull
        ReviewTargetType targetType,

        @NotNull
        Long targetId,

        @NotNull @Min(1) @Max(5)
        Integer rating,

        @Size(max = 1000)
        String content
) {
}
