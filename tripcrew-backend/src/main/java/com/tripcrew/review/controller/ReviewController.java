package com.tripcrew.review.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.tripcrew.review.model.ReviewTargetType;
import com.tripcrew.review.model.dto.ReviewCreateRequest;
import com.tripcrew.review.model.dto.ReviewResponse;
import com.tripcrew.review.service.ReviewService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * F08 후기/평점.
 * 목록 조회(GET)는 공개(SecurityConfig permitAll), 작성(POST)은 인증 필요.
 */
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /** 특정 대상(폴리모픽)의 후기 목록. 예: /api/reviews?targetType=ATTRACTION&targetId=123 */
    @GetMapping
    public List<ReviewResponse> list(@RequestParam ReviewTargetType targetType,
                                     @RequestParam Long targetId) {
        return reviewService.listByTarget(targetType, targetId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewResponse create(@AuthenticationPrincipal Long userId,
                                 @Valid @RequestBody ReviewCreateRequest request) {
        return reviewService.create(userId, request);
    }
}
