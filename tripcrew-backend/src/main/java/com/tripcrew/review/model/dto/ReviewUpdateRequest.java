package com.tripcrew.review.model.dto;

import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 후기 수정 요청. 대상(targetType/targetId)은 바꿀 수 없으므로 받지 않는다(평점/내용만 수정).
 * 본인 여부는 서비스에서 검증한다.
 *
 * <p>{@code imageUrls} 는 수정 후 최종 이미지 목록(전체 교체, 최대 5장). 기존에서 빠진 이미지는 삭제,
 * 새로 추가된 URL 은 업로드 엔드포인트로 미리 올려 받은 공개 URL 이어야 한다(서비스에서 검증).
 */
public record ReviewUpdateRequest(

        @NotNull @Min(1) @Max(5)
        Integer rating,

        @Size(max = 1000)
        String content,

        @Size(max = 5, message = "이미지는 최대 5장까지 첨부할 수 있습니다.")
        List<String> imageUrls
) {
}
