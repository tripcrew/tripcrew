package com.tripcrew.like.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tripcrew.like.dto.LikeStatusResponse;
import com.tripcrew.like.service.AttractionLikeService;

import lombok.RequiredArgsConstructor;

/**
 * 관광지 좋아요(찜) API.
 *   GET    /api/attractions/{no}/likes  좋아요 상태(공개 — GET /api/attractions/** permitAll 로 커버)
 *   POST   /api/attractions/{no}/likes  좋아요(인증)
 *   DELETE /api/attractions/{no}/likes  좋아요 취소(인증)
 *
 * <p>관광지 본 도메인(AttractionController)과 분리된 별도 모듈로 둔다(관심사 분리).
 * 모든 응답은 최신 {@link LikeStatusResponse}(liked + likeCount).
 */
@RestController
@RequestMapping("/api/attractions/{no}/likes")
@RequiredArgsConstructor
public class AttractionLikeController {

    private final AttractionLikeService likeService;

    /** 비로그인도 호출 가능(liked=false). 토큰이 있으면 JWT 필터가 userId 를 채워준다. */
    @GetMapping
    public LikeStatusResponse status(@AuthenticationPrincipal Long userId,
                                     @PathVariable Integer no) {
        return likeService.getStatus(userId, no);
    }

    @PostMapping
    public LikeStatusResponse like(@AuthenticationPrincipal Long userId,
                                   @PathVariable Integer no) {
        return likeService.like(userId, no);
    }

    @DeleteMapping
    public LikeStatusResponse unlike(@AuthenticationPrincipal Long userId,
                                     @PathVariable Integer no) {
        return likeService.unlike(userId, no);
    }
}
