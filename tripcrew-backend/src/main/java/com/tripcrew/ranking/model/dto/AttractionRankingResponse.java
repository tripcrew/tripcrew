package com.tripcrew.ranking.model.dto;

/** Redis 최근 1시간 ZSet 점수와 MySQL 관광지 정보를 합친 랭킹 응답. */
public record AttractionRankingResponse(
        int rank,
        Integer id,
        String title,
        String region,
        String imageUrl,
        long score,
        String trend,
        int delta
) {
}
