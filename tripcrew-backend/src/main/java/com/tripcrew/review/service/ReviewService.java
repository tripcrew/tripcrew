package com.tripcrew.review.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tripcrew.common.exception.BusinessException;
import com.tripcrew.review.model.ReviewTargetType;
import com.tripcrew.review.model.dto.Review;
import com.tripcrew.review.model.dto.ReviewCreateRequest;
import com.tripcrew.review.model.dto.ReviewResponse;
import com.tripcrew.review.model.mapper.ReviewMapper;

import lombok.RequiredArgsConstructor;

/**
 * F08 후기/평점. 폴리모픽 대상(ATTRACTION | TRIP_PLAN)에 대한 후기를 다룬다.
 * 대상 테이블이 종류별로 달라 DB FK 를 둘 수 없으므로 대상 존재 검증은 여기(앱레벨)에서 한다.
 */
@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewMapper reviewMapper;

    /** 특정 대상의 후기 목록(최신순). 인증된 사용자라면 누구나 조회 가능. */
    @Transactional(readOnly = true)
    public List<ReviewResponse> listByTarget(ReviewTargetType targetType, Long targetId) {
        return reviewMapper.findByTarget(targetType, targetId).stream()
                .map(ReviewResponse::from)
                .toList();
    }

    /** 후기 작성. 작성자는 인증 주체. 대상 존재를 앱레벨에서 검증한다. */
    @Transactional
    public ReviewResponse create(Long userId, ReviewCreateRequest request) {
        validateTargetExists(request.targetType(), request.targetId());

        Review review = Review.builder()
                .userId(userId)
                .targetType(request.targetType())
                .targetId(request.targetId())
                .rating(request.rating())
                .content(request.content())
                .build();
        reviewMapper.insert(review);

        return ReviewResponse.from(findOrThrow(review.getId()));
    }

    private Review findOrThrow(Long id) {
        return reviewMapper.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "후기를 찾을 수 없습니다."));
    }

    /** 폴리모픽 대상이 실제로 존재하는지 종류별로 확인(없으면 400). */
    private void validateTargetExists(ReviewTargetType targetType, Long targetId) {
        boolean exists = switch (targetType) {
            case ATTRACTION -> reviewMapper.existsAttraction(targetId);
            case TRIP_PLAN -> reviewMapper.existsTripPlan(targetId);
        };
        if (!exists) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "후기 대상이 존재하지 않습니다.");
        }
    }
}
