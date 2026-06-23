package com.tripcrew.review.model.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.tripcrew.review.model.ReviewStatus;
import com.tripcrew.review.model.ReviewTargetType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * reviews 테이블 매핑. (MyBatis 가 setter 로 채우므로 기본생성자/세터 필요)
 * created_at / updated_at 은 DB DEFAULT 로 채워지므로 INSERT 시 다루지 않는다.
 *
 * <p>{@code authorNickname} 은 reviews 컬럼이 아니라 목록 조회 시 users 조인으로
 * 채워지는 값이다(작성자 표시용). 단건 INSERT 후 재조회에서는 채워지지 않을 수 있다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    private Long id;
    private Long userId;
    private ReviewTargetType targetType;
    private Long targetId;
    private Integer rating;
    private String content;
    private ReviewStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** 조인으로 채우는 작성자 닉네임(reviews 컬럼 아님). */
    private String authorNickname;

    /** 별도 조회(review_images)로 채우는 첨부 이미지 URL 목록(reviews 컬럼 아님). */
    private List<String> imageUrls;
}
