package com.tripcrew.notice.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tripcrew.notice.model.dto.NoticeResponse;
import com.tripcrew.notice.service.NoticeService;

import lombok.RequiredArgsConstructor;

/**
 * F10 공지사항(공개 조회). 목록/상세 모두 공개(SecurityConfig permitAll).
 * 상세 조회 시 조회수가 +1 된다.
 */
@RestController
@RequestMapping("/api/notices")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    /** 공지 목록(고정 우선·최신순). */
    @GetMapping
    public List<NoticeResponse> list() {
        return noticeService.list();
    }

    /** 공지 상세. 조회할 때마다 조회수 +1. */
    @GetMapping("/{id}")
    public NoticeResponse detail(@PathVariable Long id) {
        return noticeService.getAndIncreaseView(id);
    }
}
