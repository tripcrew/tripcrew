package com.tripcrew.review.model.dto;

import java.time.LocalDateTime;

import com.tripcrew.review.model.ReviewTargetType;

/**
 * 후기 응답. 작성자 식별(userId)과 표시명(authorNickname)을 함께 내려
 * 프론트가 별도 조회 없이 목록을 렌더링할 수 있게 한다.
 */
public record ReviewResponse(
        Long id,
        Long userId,
        String authorNickname,
        ReviewTargetType targetType,
        Long targetId,
        Integer rating,
        String content,
        LocalDateTime createdAt
) {
    public static ReviewResponse from(Review review) {
        return new ReviewResponse(
                review.getId(),
                review.getUserId(),
                review.getAuthorNickname(),
                review.getTargetType(),
                review.getTargetId(),
                review.getRating(),
                review.getContent(),
                review.getCreatedAt()
        );
    }
}
