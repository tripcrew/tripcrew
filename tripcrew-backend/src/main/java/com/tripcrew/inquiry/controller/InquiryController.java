package com.tripcrew.inquiry.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.tripcrew.inquiry.model.dto.InquiryCreateRequest;
import com.tripcrew.inquiry.model.dto.InquiryResponse;
import com.tripcrew.inquiry.service.InquiryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 로드맵 ③ 1:1 문의(사용자). 모두 인증 필요(SecurityConfig anyRequest authenticated 로 커버).
 *   POST /api/inquiries       문의 작성
 *   GET  /api/me/inquiries    내 문의 목록(답변 포함, 본인 스코프)
 */
@RestController
@RequiredArgsConstructor
public class InquiryController {

    private final InquiryService inquiryService;

    @PostMapping("/api/inquiries")
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@AuthenticationPrincipal Long userId,
                       @Valid @RequestBody InquiryCreateRequest request) {
        inquiryService.create(userId, request);
    }

    @GetMapping("/api/me/inquiries")
    public List<InquiryResponse> myList(@AuthenticationPrincipal Long userId) {
        return inquiryService.myList(userId);
    }
}
