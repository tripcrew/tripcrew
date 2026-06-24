package com.tripcrew.notification.model.dto;

import java.time.LocalDateTime;

import com.tripcrew.notification.model.NotificationType;

/**
 * 알림 목록 응답 한 건. 수신자(userId)는 내 알림만 돌려주므로 노출하지 않는다.
 * 프론트는 {@code type}+{@code refId} 로 클릭 시 이동 경로를 분기한다.
 */
public record NotificationResponse(
        Long id,
        NotificationType type,
        Long refId,
        String message,
        boolean read,
        LocalDateTime createdAt
) {
    public static NotificationResponse from(Notification n) {
        return new NotificationResponse(
                n.getId(), n.getType(), n.getRefId(), n.getMessage(), n.isRead(), n.getCreatedAt());
    }
}
