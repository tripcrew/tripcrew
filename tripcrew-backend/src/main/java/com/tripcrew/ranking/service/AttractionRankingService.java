package com.tripcrew.ranking.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import com.tripcrew.attraction.model.dto.AttractionDetailResponse;
import com.tripcrew.attraction.model.mapper.AttractionMapper;
import com.tripcrew.ranking.model.dto.AttractionRankingResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AttractionRankingService {

    private static final int RANKING_LIMIT = 5;
    private static final ZoneId KOREA_ZONE = ZoneId.of("Asia/Seoul");
    private static final DateTimeFormatter KEY_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
    private static final String MINUTE_KEY_PREFIX = "ranking:attractions:";
    private static final String ROLLING_KEY = "ranking:attractions:rolling";
    private static final String PREVIOUS_ROLLING_KEY = "ranking:attractions:previous-rolling";

    private final StringRedisTemplate redisTemplate;
    private final AttractionMapper attractionMapper;

    /** 관광지 상세 조회 1회를 현재 분 ZSet에 1점으로 기록한다. */
    public void recordDetailView(Integer attractionId) {
        String key = minuteKey(LocalDateTime.now(KOREA_ZONE));
        redisTemplate.opsForZSet().incrementScore(key, attractionId.toString(), 1);
        // 현재 1시간과 직전 1시간의 순위를 비교할 수 있게 2시간보다 조금 길게 보관한다.
        redisTemplate.expire(key, Duration.ofMinutes(130));
    }

    /** 최근 60개 분 단위 ZSet을 합산해 상위 5개 관광지를 반환한다. */
    public List<AttractionRankingResponse> getTopFive() {
        buildRollingRankingsIfNeeded();

        Set<ZSetOperations.TypedTuple<String>> rankedItems = redisTemplate.opsForZSet()
                .reverseRangeWithScores(ROLLING_KEY, 0, RANKING_LIMIT - 1);
        if (rankedItems == null || rankedItems.isEmpty()) {
            return List.of();
        }

        List<AttractionRankingResponse> result = new ArrayList<>();
        int rank = 1;
        for (ZSetOperations.TypedTuple<String> item : rankedItems) {
            String value = item.getValue();
            if (value == null) {
                continue;
            }

            AttractionDetailResponse attraction = attractionMapper.findByNo(Integer.valueOf(value)).orElse(null);
            if (attraction == null) {
                continue;
            }

            long score = Math.round(item.getScore() == null ? 0 : item.getScore());
            RankTrend trend = trendOf(rank, value);
            result.add(new AttractionRankingResponse(
                    rank++,
                    attraction.getNo(),
                    cleanDisplayName(attraction.getTitle()),
                    regionOf(attraction),
                    attraction.getFirstImage1(),
                    score,
                    trend.label(),
                    trend.delta()
            ));
        }
        return result;
    }

    private RankTrend trendOf(int currentRank, String attractionId) {
        Long previousZeroBasedRank = redisTemplate.opsForZSet().reverseRank(PREVIOUS_ROLLING_KEY, attractionId);
        if (previousZeroBasedRank == null) {
            return new RankTrend("new", 0);
        }

        int delta = (int) (previousZeroBasedRank + 1 - currentRank);
        if (delta > 0) {
            return new RankTrend("up", delta);
        }
        if (delta < 0) {
            return new RankTrend("down", Math.abs(delta));
        }
        return new RankTrend("same", 0);
    }

    private void buildRollingRankingsIfNeeded() {
        buildRankingIfNeeded(ROLLING_KEY, minuteKeysFromNow(0));
        buildRankingIfNeeded(PREVIOUS_ROLLING_KEY, minuteKeysFromNow(60));
    }

    private void buildRankingIfNeeded(String resultKey, List<String> keys) {
        // 같은 5초 안의 요청은 동일한 집계 결과를 재사용한다.
        if (Boolean.TRUE.equals(redisTemplate.hasKey(resultKey))) {
            return;
        }
        redisTemplate.opsForZSet().unionAndStore(keys.get(0), keys.subList(1, keys.size()), resultKey);
        redisTemplate.expire(resultKey, Duration.ofSeconds(5));
    }

    private List<String> minuteKeysFromNow(int firstMinuteOffset) {
        LocalDateTime now = LocalDateTime.now(KOREA_ZONE).withSecond(0).withNano(0);
        return IntStream.range(firstMinuteOffset, firstMinuteOffset + 60)
                .mapToObj(offset -> now.minusMinutes(offset))
                .map(this::minuteKey)
                .toList();
    }

    private String minuteKey(LocalDateTime time) {
        return MINUTE_KEY_PREFIX + time.format(KEY_TIME_FORMAT);
    }

    private String regionOf(AttractionDetailResponse attraction) {
        return Stream.of(attraction.getSido(), attraction.getGugun())
                .filter(value -> value != null && !value.isBlank())
                .reduce((left, right) -> left + " " + right)
                .orElse("지역 정보 없음");
    }

    private String cleanDisplayName(String value) {
        if (value == null) {
            return "";
        }
        return value.trim()
                .replaceAll("(?:\\s+\\(?#?\\d{5,}\\)?)+\\s*$", "")
                .replaceAll("^\\s*(?:\\(?#?\\d{5,}\\)?\\s+)+", "");
    }

    private record RankTrend(String label, int delta) {
    }
}
