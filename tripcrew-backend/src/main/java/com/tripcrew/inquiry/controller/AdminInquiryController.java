package com.tripcrew.inquiry.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.tripcrew.inquiry.model.InquiryStatus;
import com.tripcrew.inquiry.model.dto.AdminInquiryResponse;
import com.tripcrew.inquiry.model.dto.InquiryAnswerRequest;
import com.tripcrew.inquiry.service.AdminInquiryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 로드맵 ③ 1:1 문의(관리자). /api/admin/** 은 SecurityConfig 에서 ROLE_ADMIN 전용
 * (별도 인가 규칙·SecurityConfig 변경 불필요).
 *   GET   /api/admin/inquiries?status=    문의 목록(작성자 이메일 포함, 보통 OPEN 미답변)
 *   PATCH /api/admin/inquiries/{id}/answer 답변 등록/수정(최초 답변 시 작성자에게 알림)
 */
@RestController
@RequestMapping("/api/admin/inquiries")
@RequiredArgsConstructor
public class AdminInquiryController {

    private final AdminInquiryService adminInquiryService;

    @GetMapping
    public List<AdminInquiryResponse> list(@RequestParam(required = false) InquiryStatus status) {
        return adminInquiryService.list(status);
    }

    @PatchMapping("/{id}/answer")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void answer(@AuthenticationPrincipal Long adminId,
                       @PathVariable Long id,
                       @Valid @RequestBody InquiryAnswerRequest request) {
        adminInquiryService.answer(id, adminId, request.answer());
    }
}
