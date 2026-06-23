package com.tripcrew.review.model.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * review_images 행 매핑. 목록 조회 시 여러 후기의 이미지를 한 번에 배치 로드할 때
 * {@code reviewId} 로 그룹핑하기 위해 함께 내려받는다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReviewImage {
    private Long reviewId;
    private String imageUrl;
    private Integer sortOrder;
}
