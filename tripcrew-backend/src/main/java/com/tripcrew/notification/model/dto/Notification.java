package com.tripcrew.notification.model.dto;

import java.time.LocalDateTime;

import com.tripcrew.notification.model.NotificationType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * notifications 테이블 매핑. (MyBatis 가 setter 로 채우므로 기본생성자/세터 필요)
 *
 * <p>{@code is_read}·{@code created_at} 은 DB DEFAULT 로 채워지므로 INSERT 시 다루지 않는다.
 * boolean 필드는 {@code read} 로 두고(Lombok 이 {@code isRead()}/{@code setRead()} 생성),
 * 컬럼 {@code is_read} → 프로퍼티 {@code read} 매핑은 XML resultMap 에서 명시한다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    private Long id;
    private Long userId;
    private NotificationType type;
    private Long refId;
    private String message;
    private boolean read;
    private LocalDateTime createdAt;
}
