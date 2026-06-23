package com.tripcrew.review.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.tripcrew.review.model.ReviewTargetType;
import com.tripcrew.review.model.dto.ReviewCreateRequest;
import com.tripcrew.review.model.dto.ReviewListResponse;
import com.tripcrew.review.model.dto.ReviewResponse;
import com.tripcrew.review.model.dto.ReviewUpdateRequest;
import com.tripcrew.review.service.ReviewService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * F08 후기/평점.
 * 목록 조회(GET)는 공개(SecurityConfig permitAll), 작성/수정/삭제는 인증 필요(수정·삭제는 본인만).
 */
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * 특정 대상(폴리모픽)의 후기 목록(페이징+정렬) + 평점 요약.
     * 예: /api/reviews?targetType=ATTRACTION&targetId=123&page=0&size=10&sort=LATEST
     */
    @GetMapping
    public ReviewListResponse list(@RequestParam ReviewTargetType targetType,
                                   @RequestParam Long targetId,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "10") int size,
                                   @RequestParam(defaultValue = "LATEST") String sort) {
        return reviewService.listByTarget(targetType, targetId, page, size, sort);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewResponse create(@AuthenticationPrincipal Long userId,
                                 @Valid @RequestBody ReviewCreateRequest request) {
        return reviewService.create(userId, request);
    }

    /** 후기 수정(본인만 — 아니면 403). */
    @PutMapping("/{id}")
    public ReviewResponse update(@AuthenticationPrincipal Long userId,
                                 @PathVariable Long id,
                                 @Valid @RequestBody ReviewUpdateRequest request) {
        return reviewService.update(userId, id, request);
    }

    /** 후기 삭제(본인만 — 아니면 403). */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@AuthenticationPrincipal Long userId,
                       @PathVariable Long id) {
        reviewService.delete(userId, id);
    }
}
