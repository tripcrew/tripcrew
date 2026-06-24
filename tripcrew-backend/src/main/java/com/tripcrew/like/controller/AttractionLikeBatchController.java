package com.tripcrew.like.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tripcrew.like.dto.AttractionLikeCount;
import com.tripcrew.like.service.AttractionLikeService;

import lombok.RequiredArgsConstructor;

/**
 * 관광지 카드용 배치 좋아요(찜) 통계.
 *   GET /api/attractions/likes/counts?nos=1,2,3
 *     → [{ no, likeCount, liked }]  (찜 0건 관광지는 생략될 수 있음 → 프론트 0/false 기본)
 *
 * <p>공개 조회(검색 결과는 비로그인도 보므로). {@code /api/attractions/**} GET permitAll 로 커버되며
 * 경로가 3세그먼트라 {@code /api/attractions/{no}} · {@code /api/attractions/{no}/likes} 와 충돌하지 않는다.
 * 토큰이 있으면 JWT 필터가 userId 를 채워 liked 를 계산한다(없으면 모두 false).
 */
@RestController
@RequiredArgsConstructor
public class AttractionLikeBatchController {

    private final AttractionLikeService likeService;

    @GetMapping("/api/attractions/likes/counts")
    public List<AttractionLikeCount> counts(@AuthenticationPrincipal Long userId,
                                            @RequestParam(name = "nos", required = false) List<Integer> nos) {
        return likeService.listLikeCounts(userId, nos);
    }
}
