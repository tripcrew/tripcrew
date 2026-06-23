package com.tripcrew.review.model.dto;

import com.tripcrew.review.model.ReviewTargetType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * review_stats 테이블 매핑. 대상(폴리모픽)별 후기 평점 비정규화 집계.
 * VISIBLE 후기만 집계하며 create/update/delete/hide 트랜잭션 안에서 증분 갱신된다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewStats {

    private ReviewTargetType targetType;
    private Long targetId;
    private long reviewCount;
    private long ratingSum;
    private double avgRating;
}
