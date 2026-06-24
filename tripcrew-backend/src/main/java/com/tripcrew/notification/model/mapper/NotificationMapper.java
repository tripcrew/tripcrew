package com.tripcrew.notification.model.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.tripcrew.notification.model.dto.Notification;

@Mapper
public interface NotificationMapper {

    /** 알림 적재. is_read·created_at 은 DB DEFAULT. 생성된 id 는 dto 에 채워진다. */
    int insert(Notification notification);

    /** 내 알림 최근 N개(최신순). */
    List<Notification> findByUser(@Param("userId") Long userId, @Param("limit") int limit);

    /** 내 미읽음 알림 개수(뱃지). */
    long countUnread(@Param("userId") Long userId);

    /** 알림 한 건 읽음 처리(본인 것만). @return 영향 행 수(0이면 없거나 내 것이 아님). */
    int markRead(@Param("id") Long id, @Param("userId") Long userId);

    /** 내 미읽음 알림 전부 읽음 처리. @return 읽음으로 바뀐 행 수. */
    int markAllRead(@Param("userId") Long userId);

    /** 알림 한 건 삭제(본인 것만). @return 영향 행 수(0이면 없거나 내 것이 아님). */
    int deleteByIdAndUser(@Param("id") Long id, @Param("userId") Long userId);
}
