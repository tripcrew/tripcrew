package com.tripcrew.review.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tripcrew.common.exception.BusinessException;
import com.tripcrew.review.model.ReviewStatus;
import com.tripcrew.review.model.ReviewTargetType;
import com.tripcrew.review.model.dto.RatingCount;
import com.tripcrew.review.model.dto.Review;
import com.tripcrew.review.model.dto.ReviewCreateRequest;
import com.tripcrew.review.model.dto.ReviewListResponse;
import com.tripcrew.review.model.dto.ReviewResponse;
import com.tripcrew.review.model.dto.ReviewStats;
import com.tripcrew.review.model.dto.ReviewUpdateRequest;
import com.tripcrew.review.model.mapper.ReviewMapper;

import lombok.RequiredArgsConstructor;

/**
 * F08 후기/평점. 폴리모픽 대상(ATTRACTION | TRIP_PLAN)에 대한 후기를 다룬다.
 * 대상 테이블이 종류별로 달라 DB FK 를 둘 수 없으므로 대상 존재 검증은 여기(앱레벨)에서 한다.
 *
 * <p><b>평점 비정규화</b>: 대상별 집계(평균/개수)는 review_stats 에 비정규화해 두고
 * create/update/delete/hide 트랜잭션 안에서 증분 갱신한다(VISIBLE 후기만 집계).
 * 한 사용자가 같은 대상에 여러 후기를 쓰는 것은 허용한다(개수 제한 없음).
 */
@Service
@RequiredArgsConstructor
public class ReviewService {

    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 50;

    private final ReviewMapper reviewMapper;

    /**
     * 특정 대상의 후기 목록(페이징+정렬) + 평점 요약. 공개 조회.
     *
     * @param sort LATEST(기본) | RATING_HIGH | RATING_LOW (대소문자 무시, 그 외는 LATEST)
     */
    @Transactional(readOnly = true)
    public ReviewListResponse listByTarget(ReviewTargetType targetType, Long targetId,
                                           int page, int size, String sort) {
        int safePage = Math.max(0, page);
        int safeSize = size <= 0 ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
        int offset = safePage * safeSize;
        String safeSort = normalizeSort(sort);

        List<ReviewResponse> content = reviewMapper
                .findByTargetPaged(targetType, targetId, safeSort, offset, safeSize)
                .stream().map(ReviewResponse::from).toList();

        long total = reviewMapper.countByTarget(targetType, targetId);
        int totalPages = safeSize == 0 ? 0 : (int) Math.ceil((double) total / safeSize);

        ReviewStats stats = reviewMapper.findStats(targetType, targetId).orElse(null);
        double average = stats != null ? stats.getAvgRating() : 0.0;

        // 별점 분포: 1~5 를 0 으로 채운 뒤 실제 집계로 덮어쓴다(없는 별점은 0 유지).
        Map<Integer, Long> distribution = new LinkedHashMap<>();
        for (int star = 5; star >= 1; star--) {
            distribution.put(star, 0L);
        }
        for (RatingCount rc : reviewMapper.findDistribution(targetType, targetId)) {
            distribution.put(rc.getRating(), rc.getCount());
        }

        return new ReviewListResponse(content, safePage, safeSize, total, totalPages,
                new ReviewListResponse.Summary(average, total, distribution));
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

        // 새 후기는 VISIBLE → 집계 +1, 평점합 +rating
        reviewMapper.applyStatsDelta(request.targetType(), request.targetId(), 1, request.rating());

        return ReviewResponse.from(findOrThrow(review.getId()));
    }

    /** 후기 수정(본인만). 평점/내용만 바꾸며 대상은 못 바꾼다. 평점 변동분만큼 집계 재계산. */
    @Transactional
    public ReviewResponse update(Long userId, Long reviewId, ReviewUpdateRequest request) {
        Review review = findOrThrow(reviewId);
        requireOwner(review, userId);

        int oldRating = review.getRating();
        reviewMapper.update(reviewId, request.rating(), request.content());

        // 집계는 VISIBLE 후기만 반영. HIDDEN(신고로 숨김) 후기는 애초에 집계에 없으므로 건드리지 않는다.
        if (review.getStatus() == ReviewStatus.VISIBLE) {
            int ratingDelta = request.rating() - oldRating;
            if (ratingDelta != 0) {
                reviewMapper.applyStatsDelta(review.getTargetType(), review.getTargetId(), 0, ratingDelta);
            }
        }

        return ReviewResponse.from(findOrThrow(reviewId));
    }

    /** 후기 삭제(본인만, 하드삭제). VISIBLE 이었다면 집계 -1, 평점합 -rating. */
    @Transactional
    public void delete(Long userId, Long reviewId) {
        Review review = findOrThrow(reviewId);
        requireOwner(review, userId);

        reviewMapper.deleteById(reviewId);

        if (review.getStatus() == ReviewStatus.VISIBLE) {
            reviewMapper.applyStatsDelta(review.getTargetType(), review.getTargetId(),
                    -1, -review.getRating());
        }
    }

    /**
     * 신고 처리완료(RESOLVED) 시 후기를 숨김 처리하고 집계에서 제외한다.
     * 관리자 신고 처리({@code AdminReportService.resolve})의 트랜잭션 안에서 호출되어 원자적으로 묶인다.
     * 이미 HIDDEN 이거나 없는 후기면 아무것도 하지 않는다(멱등 — 집계 중복 차감 방지).
     */
    @Transactional
    public void hideForReport(Long reviewId) {
        Review review = reviewMapper.findById(reviewId).orElse(null);
        if (review == null || review.getStatus() != ReviewStatus.VISIBLE) {
            return;
        }
        reviewMapper.hideById(reviewId);
        reviewMapper.applyStatsDelta(review.getTargetType(), review.getTargetId(),
                -1, -review.getRating());
    }

    private Review findOrThrow(Long id) {
        return reviewMapper.findById(id)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "후기를 찾을 수 없습니다."));
    }

    /** 후기 작성자 본인이 아니면 403. */
    private void requireOwner(Review review, Long userId) {
        if (!review.getUserId().equals(userId)) {
            throw new BusinessException(HttpStatus.FORBIDDEN, "본인이 작성한 후기만 수정/삭제할 수 있습니다.");
        }
    }

    /** 정렬 키 화이트리스트(SQL 주입 방지). 허용값 외엔 LATEST. */
    private String normalizeSort(String sort) {
        if (sort == null) {
            return "LATEST";
        }
        return switch (sort.trim().toUpperCase()) {
            case "RATING_HIGH" -> "RATING_HIGH";
            case "RATING_LOW" -> "RATING_LOW";
            default -> "LATEST";
        };
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
