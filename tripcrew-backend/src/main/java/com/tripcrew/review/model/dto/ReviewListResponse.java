package com.tripcrew.review.model.dto;

import java.util.List;
import java.util.Map;

/**
 * 후기 목록 응답(페이징 + 평점 요약).
 *
 * <p>{@code summary} 는 페이지와 무관하게 대상 전체(VISIBLE)의 집계를 담는다:
 * 평균({@code average})·총개수({@code count})·별점 분포({@code distribution}, 키 1~5).
 * 평균/개수는 비정규화 테이블(review_stats)에서, 분포는 조회 시 집계해 채운다.
 */
public record ReviewListResponse(
        List<ReviewResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        Summary summary
) {

    public record Summary(
            double average,
            long count,
            Map<Integer, Long> distribution
    ) {
    }
}
