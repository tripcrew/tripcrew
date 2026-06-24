package com.tripcrew.notification.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.tripcrew.notification.model.dto.NotificationResponse;
import com.tripcrew.notification.model.dto.UnreadCountResponse;
import com.tripcrew.notification.service.NotificationService;

import lombok.RequiredArgsConstructor;

/**
 * 내 알림. 모두 인증 필요(SecurityConfig anyRequest authenticated 로 커버 — 별도 규칙 없음).
 *   GET   /api/me/notifications               최근 알림 목록(최신순)
 *   GET   /api/me/notifications/unread-count  미읽음 개수(벨 뱃지)
 *   PATCH /api/me/notifications/{id}/read     한 건 읽음
 *   PATCH /api/me/notifications/read-all      전부 읽음
 */
@RestController
@RequestMapping("/api/me/notifications")
@RequiredArgsConstructor
public class MyNotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public List<NotificationResponse> list(@AuthenticationPrincipal Long userId) {
        return notificationService.list(userId);
    }

    @GetMapping("/unread-count")
    public UnreadCountResponse unreadCount(@AuthenticationPrincipal Long userId) {
        return new UnreadCountResponse(notificationService.unreadCount(userId));
    }

    @PatchMapping("/{id}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markRead(@AuthenticationPrincipal Long userId, @PathVariable Long id) {
        notificationService.markRead(userId, id);
    }

    @PatchMapping("/read-all")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markAllRead(@AuthenticationPrincipal Long userId) {
        notificationService.markAllRead(userId);
    }
}
