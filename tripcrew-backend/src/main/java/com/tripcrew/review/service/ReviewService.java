package com.tripcrew.review.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tripcrew.common.exception.BusinessException;
import com.tripcrew.restriction.model.RestrictionType;
import com.tripcrew.restriction.service.RestrictionService;
import com.tripcrew.review.model.ReviewStatus;
import com.tripcrew.review.model.ReviewTargetType;
import com.tripcrew.review.model.dto.RatingCount;
import com.tripcrew.review.model.dto.Review;
import com.tripcrew.review.model.dto.ReviewCreateRequest;
import com.tripcrew.review.model.dto.ReviewImage;
import com.tripcrew.review.model.dto.ReviewListResponse;
import com.tripcrew.review.model.dto.ReviewResponse;
import com.tripcrew.review.model.dto.ReviewStats;
import com.tripcrew.review.model.dto.ReviewUpdateRequest;
import com.tripcrew.review.model.mapper.ReviewMapper;
import com.tripcrew.upload.service.FileStorageService;

import lombok.RequiredArgsConstructor;

/**
 * F08 후기/평점. 폴리모픽 대상(ATTRACTION | TRIP_PLAN)에 대한 후기를 다룬다.
 * 대상 테이블이 종류별로 달라 DB FK 를 둘 수 없으므로 대상 존재 검증은 여기(앱레벨)에서 한다.
 *
 * <p><b>평점 비정규화</b>: 대상별 집계(평균/개수)는 review_stats 에 비정규화해 두고
 * create/update/delete/hide 트랜잭션 안에서 증분 갱신한다(VISIBLE 후기만 집계).
 * 한 사용자가 같은 대상에 여러 후기를 쓰는 것은 허용한다(개수 제한 없음).
 *
 * <p><b>이미지</b>: 후기당 최대 5장(review_images). URL 은 업로드 엔드포인트가 내려준
 * {@code /uploads/reviews/...} 형식만 허용한다(외부 URL·경로 탈출 차단). 수정은 전체 교체.
 */
@Service
@RequiredArgsConstructor
public class ReviewService {

    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 50;
    private static final int MAX_IMAGES = 5;

    /** 업로드 엔드포인트가 후기 이미지를 저장하는 위치(review 하위)와 일치해야 한다. */
    private static final Pattern IMAGE_URL_PATTERN =
            Pattern.compile("^" + Pattern.quote(FileStorageService.PUBLIC_PREFIX)
                    + "reviews/[A-Za-z0-9]+\\.(jpg|png|webp|gif)$");

    private final ReviewMapper reviewMapper;
    private final FileStorageService fileStorageService;
    private final RestrictionService restrictionService;

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

        List<Review> reviews = reviewMapper.findByTargetPaged(targetType, targetId, safeSort, offset, safeSize);
        attachImages(reviews);
        List<ReviewResponse> content = reviews.stream().map(ReviewResponse::from).toList();

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

    /** 후기 작성. 작성자는 인증 주체. 대상 존재를 앱레벨에서 검증한다. 이미지는 최대 5장. */
    @Transactional
    public ReviewResponse create(Long userId, ReviewCreateRequest request) {
        // 신고 누적 단계 제재(후기 작성 금지) 중이면 403.
        restrictionService.requireAllowed(userId, RestrictionType.REVIEW_WRITE);
        validateTargetExists(request.targetType(), request.targetId());
        List<String> imageUrls = sanitizeImageUrls(request.imageUrls());

        Review review = Review.builder()
                .userId(userId)
                .targetType(request.targetType())
                .targetId(request.targetId())
                .rating(request.rating())
                .content(request.content())
                .build();
        reviewMapper.insert(review);

        if (!imageUrls.isEmpty()) {
            reviewMapper.insertImages(review.getId(), imageUrls);
        }

        // 새 후기는 VISIBLE → 집계 +1, 평점합 +rating
        reviewMapper.applyStatsDelta(request.targetType(), request.targetId(), 1, request.rating());

        Review saved = findOrThrow(review.getId());
        saved.setImageUrls(imageUrls);
        return ReviewResponse.from(saved);
    }

    /**
     * 후기 수정(본인만). 평점/내용과 이미지(전체 교체)를 바꾸며 대상은 못 바꾼다.
     * 평점 변동분만큼 집계 재계산. 이미지는 새 목록으로 교체하고, 빠진 파일은 삭제한다.
     */
    @Transactional
    public ReviewResponse update(Long userId, Long reviewId, ReviewUpdateRequest request) {
        Review review = findOrThrow(reviewId);
        requireOwner(review, userId);
        List<String> newImageUrls = sanitizeImageUrls(request.imageUrls());

        int oldRating = review.getRating();
        reviewMapper.update(reviewId, request.rating(), request.content());

        // 집계는 VISIBLE 후기만 반영. HIDDEN(신고로 숨김) 후기는 애초에 집계에 없으므로 건드리지 않는다.
        if (review.getStatus() == ReviewStatus.VISIBLE) {
            int ratingDelta = request.rating() - oldRating;
            if (ratingDelta != 0) {
                reviewMapper.applyStatsDelta(review.getTargetType(), review.getTargetId(), 0, ratingDelta);
            }
        }

        // 이미지 전체 교체: 기존 행 삭제 후 새로 삽입. 목록에서 빠진 파일은 실제로 지운다.
        List<String> oldImageUrls = reviewMapper.findImageUrlsByReviewId(reviewId);
        reviewMapper.deleteImagesByReviewId(reviewId);
        if (!newImageUrls.isEmpty()) {
            reviewMapper.insertImages(reviewId, newImageUrls);
        }
        for (String removed : oldImageUrls) {
            if (!newImageUrls.contains(removed)) {
                fileStorageService.delete(removed);
            }
        }

        Review saved = findOrThrow(reviewId);
        saved.setImageUrls(newImageUrls);
        return ReviewResponse.from(saved);
    }

    /** 후기 삭제(본인만, 하드삭제). VISIBLE 이었다면 집계 -1, 평점합 -rating. 이미지 파일도 정리. */
    @Transactional
    public void delete(Long userId, Long reviewId) {
        Review review = findOrThrow(reviewId);
        requireOwner(review, userId);

        // 파일 경로를 행 삭제 전에 확보(review_images 는 FK CASCADE 로 함께 삭제됨).
        List<String> imageUrls = reviewMapper.findImageUrlsByReviewId(reviewId);

        reviewMapper.deleteById(reviewId);

        if (review.getStatus() == ReviewStatus.VISIBLE) {
            reviewMapper.applyStatsDelta(review.getTargetType(), review.getTargetId(),
                    -1, -review.getRating());
        }
        imageUrls.forEach(fileStorageService::delete);
    }

    /**
     * 신고 처리완료(RESOLVED) 시 후기를 숨김 처리하고 집계에서 제외한다.
     * 관리자 신고 처리({@code AdminReportService.resolve})의 트랜잭션 안에서 호출되어 원자적으로 묶인다.
     * 이미 HIDDEN 이거나 없는 후기면 아무것도 하지 않는다(멱등 — 집계 중복 차감 방지).
     * 이미지는 row 보존(soft-delete)이라 파일을 지우지 않는다(공개 목록에서만 빠진다).
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

    /** 여러 후기에 이미지 목록을 한 번의 조회로 채운다(N+1 방지). */
    private void attachImages(List<Review> reviews) {
        if (reviews.isEmpty()) {
            return;
        }
        List<Long> ids = reviews.stream().map(Review::getId).toList();
        Map<Long, List<String>> byReview = reviewMapper.findImagesByReviewIds(ids).stream()
                .collect(Collectors.groupingBy(ReviewImage::getReviewId,
                        Collectors.mapping(ReviewImage::getImageUrl, Collectors.toList())));
        for (Review r : reviews) {
            r.setImageUrls(byReview.getOrDefault(r.getId(), List.of()));
        }
    }

    /**
     * 이미지 URL 목록 검증/정규화. null 은 빈 목록으로, 최대 5장, 형식은
     * 업로드 엔드포인트가 내려준 {@code /uploads/reviews/...} 만 허용한다(외부 URL·경로 탈출 차단).
     */
    private List<String> sanitizeImageUrls(List<String> urls) {
        if (urls == null || urls.isEmpty()) {
            return List.of();
        }
        if (urls.size() > MAX_IMAGES) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "이미지는 최대 " + MAX_IMAGES + "장까지 첨부할 수 있습니다.");
        }
        List<String> result = new ArrayList<>(urls.size());
        for (String url : urls) {
            if (url == null || !IMAGE_URL_PATTERN.matcher(url).matches()) {
                throw new BusinessException(HttpStatus.BAD_REQUEST, "잘못된 이미지 경로입니다.");
            }
            result.add(url);
        }
        return result;
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
