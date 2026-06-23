package com.tripcrew.attraction.service;

import java.util.Map;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.tripcrew.attraction.model.dto.AttractionDetailResponse;
import com.tripcrew.attraction.model.dto.AttractionPageResponse;
import com.tripcrew.attraction.model.dto.AttractionSearchRequest;
import com.tripcrew.attraction.model.dto.AttractionSummaryResponse;
import com.tripcrew.attraction.model.mapper.AttractionMapper;
import com.tripcrew.common.exception.BusinessException;
import com.tripcrew.ranking.service.AttractionRankingService;
import com.tripcrew.review.model.ReviewTargetType;
import com.tripcrew.review.model.dto.ReviewStats;
import com.tripcrew.review.model.mapper.ReviewMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AttractionService {

    private static final long SLOW_SEARCH_THRESHOLD_MS = 500;

    private final AttractionMapper attractionMapper;
    private final ReviewMapper reviewMapper;
    private final AttractionRankingService attractionRankingService;

    public AttractionPageResponse search(AttractionSearchRequest request) {
        long startedAt = System.nanoTime();

        request.normalize();

        long searchStartedAt = System.nanoTime();
        List<AttractionSummaryResponse> items = attractionMapper.search(request);
        long searchElapsedMs = elapsedMillis(searchStartedAt);

        long reviewStatsStartedAt = System.nanoTime();
        attachReviewStats(items);
        long reviewStatsElapsedMs = elapsedMillis(reviewStatsStartedAt);

        long countStartedAt = System.nanoTime();
        long totalCount = attractionMapper.count(request);
        long countElapsedMs = elapsedMillis(countStartedAt);

        AttractionPageResponse response = AttractionPageResponse.of(items, request.getPage(), request.getLimit(), totalCount);
        long totalElapsedMs = elapsedMillis(startedAt);

        if (totalElapsedMs >= SLOW_SEARCH_THRESHOLD_MS) {
            log.warn(
                    "Slow attraction search: total={}ms, search={}ms, reviewStats={}ms, count={}ms, keyword='{}', sidoCode={}, gugunCode={}, contentTypeIds={}, page={}, size={}, resultCount={}, totalCount={}",
                    totalElapsedMs,
                    searchElapsedMs,
                    reviewStatsElapsedMs,
                    countElapsedMs,
                    request.getKeyword(),
                    request.getSidoCode(),
                    request.getGugunCode(),
                    request.getContentTypeIds(),
                    request.getPage(),
                    request.getLimit(),
                    items.size(),
                    totalCount
            );
        } else {
            log.info(
                    "Attraction search: total={}ms, search={}ms, reviewStats={}ms, count={}ms, keyword='{}', sidoCode={}, gugunCode={}, contentTypeIds={}, page={}, size={}, resultCount={}, totalCount={}",
                    totalElapsedMs,
                    searchElapsedMs,
                    reviewStatsElapsedMs,
                    countElapsedMs,
                    request.getKeyword(),
                    request.getSidoCode(),
                    request.getGugunCode(),
                    request.getContentTypeIds(),
                    request.getPage(),
                    request.getLimit(),
                    items.size(),
                    totalCount
            );
        }

        return response;
    }

    private void attachReviewStats(List<AttractionSummaryResponse> items) {
        if (items.isEmpty()) {
            return;
        }

        List<Long> attractionIds = items.stream()
                .map(AttractionSummaryResponse::getNo)
                .map(Integer::longValue)
                .toList();
        Map<Long, ReviewStats> statsByAttractionId = reviewMapper
                .findStatsByTargetIds(ReviewTargetType.ATTRACTION, attractionIds)
                .stream()
                .collect(Collectors.toMap(ReviewStats::getTargetId, Function.identity()));

        for (AttractionSummaryResponse item : items) {
            ReviewStats stats = statsByAttractionId.get(item.getNo().longValue());
            item.setReviewAverage(stats == null ? 0.0 : stats.getAvgRating());
            item.setReviewCount(stats == null ? 0L : stats.getReviewCount());
        }
    }

    public AttractionDetailResponse get(Integer no) {
        AttractionDetailResponse attraction = attractionMapper.findByNo(no)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "관광지를 찾을 수 없습니다."));

        try {
            attractionRankingService.recordDetailView(no);
        } catch (RuntimeException e) {
            // Redis 장애가 관광지 상세 조회 자체를 실패시키면 안 된다.
            log.warn("Failed to record attraction ranking. attractionId={}", no, e);
        }

        return attraction;
    }

    private long elapsedMillis(long startedAt) {
        return (System.nanoTime() - startedAt) / 1_000_000;
    }
}
