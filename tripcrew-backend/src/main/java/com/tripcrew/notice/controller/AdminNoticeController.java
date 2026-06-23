package com.tripcrew.notice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.tripcrew.notice.model.dto.NoticeCreateRequest;
import com.tripcrew.notice.model.dto.NoticeResponse;
import com.tripcrew.notice.model.dto.NoticeUpdateRequest;
import com.tripcrew.notice.service.NoticeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * F10 공지사항(관리자 작성/수정/삭제). /api/admin/** 은 SecurityConfig 에서 ROLE_ADMIN 전용.
 */
@RestController
@RequestMapping("/api/admin/notices")
@RequiredArgsConstructor
public class AdminNoticeController {

    private final NoticeService noticeService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NoticeResponse create(@AuthenticationPrincipal Long adminId,
                                 @Valid @RequestBody NoticeCreateRequest request) {
        return noticeService.create(adminId, request);
    }

    @PutMapping("/{id}")
    public NoticeResponse update(@PathVariable Long id,
                                 @Valid @RequestBody NoticeUpdateRequest request) {
        return noticeService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        noticeService.delete(id);
    }
}
