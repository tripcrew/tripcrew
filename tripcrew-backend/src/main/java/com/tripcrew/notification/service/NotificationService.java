package com.tripcrew.notification.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tripcrew.common.exception.BusinessException;
import com.tripcrew.notification.model.NotificationType;
import com.tripcrew.notification.model.dto.Notification;
import com.tripcrew.notification.model.dto.NotificationResponse;
import com.tripcrew.notification.model.mapper.NotificationMapper;

import lombok.RequiredArgsConstructor;

/**
 * 회원 알림(범용). 다른 도메인은 {@link #notify} 헬퍼로 알림을 적재하고,
 * 헤더 벨은 목록/미읽음수 조회와 읽음 처리 API 를 쓴다.
 *
 * <p>알림 적재는 보통 호출측 트랜잭션(예: 신고 처리완료) 안에서 함께 일어나
 * 트리거 동작과 알림 생성이 원자적으로 묶인다(전파 기본값).
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

    /** 목록에 한 번에 내려주는 최근 알림 개수 상한. */
    private static final int LIST_LIMIT = 30;

    private final NotificationMapper notificationMapper;

    /**
     * 알림 적재 공용 헬퍼. 트리거가 늘면 이 한 메서드로 알림을 만든다.
     *
     * @param userId  수신자 id
     * @param type    알림 유형
     * @param refId   연관 엔티티 id(없으면 null)
     * @param message 표시 문구(서버 생성, 255자 이내)
     */
    public void notify(Long userId, NotificationType type, Long refId, String message) {
        Notification notification = Notification.builder()
                .userId(userId)
                .type(type)
                .refId(refId)
                .message(message)
                .build();
        notificationMapper.insert(notification);
    }

    /** 내 알림 최근 목록(최신순). */
    @Transactional(readOnly = true)
    public List<NotificationResponse> list(Long userId) {
        return notificationMapper.findByUser(userId, LIST_LIMIT).stream()
                .map(NotificationResponse::from)
                .toList();
    }

    /** 내 미읽음 알림 개수(뱃지). */
    @Transactional(readOnly = true)
    public long unreadCount(Long userId) {
        return notificationMapper.countUnread(userId);
    }

    /** 알림 한 건 읽음 처리(본인 것만). 없거나 내 것이 아니면 404. */
    @Transactional
    public void markRead(Long userId, Long id) {
        int affected = notificationMapper.markRead(id, userId);
        if (affected == 0) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "알림을 찾을 수 없습니다.");
        }
    }

    /** 내 알림 전부 읽음 처리(멱등). */
    @Transactional
    public void markAllRead(Long userId) {
        notificationMapper.markAllRead(userId);
    }

    /** 알림 한 건 삭제(본인 것만). 없거나 내 것이 아니면 404. */
    @Transactional
    public void delete(Long userId, Long id) {
        int affected = notificationMapper.deleteByIdAndUser(id, userId);
        if (affected == 0) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "알림을 찾을 수 없습니다.");
        }
    }

    /** 내 알림 전체 삭제(멱등). */
    @Transactional
    public void deleteAll(Long userId) {
        notificationMapper.deleteAllByUser(userId);
    }
}
