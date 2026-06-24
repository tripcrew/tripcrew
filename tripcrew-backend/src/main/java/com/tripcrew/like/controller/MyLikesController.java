package com.tripcrew.like.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tripcrew.like.dto.WishlistItemResponse;
import com.tripcrew.like.service.AttractionLikeService;

import lombok.RequiredArgsConstructor;

/**
 * 내 찜(가보고 싶어요) 목록. 인증 필요(anyRequest authenticated 로 커버).
 *   GET /api/me/likes  내가 찜한 관광지 목록(카드 + 평점 + 총 찜 수, 최근 찜 순)
 */
@RestController
@RequestMapping("/api/me/likes")
@RequiredArgsConstructor
public class MyLikesController {

    private final AttractionLikeService likeService;

    @GetMapping
    public List<WishlistItemResponse> myLikes(@AuthenticationPrincipal Long userId) {
        return likeService.listMine(userId);
    }
}
