package com.tripcrew.notice.model.dto;

import java.time.LocalDateTime;

/**
 * 공지 응답(목록·상세 공용). 작성 관리자 표시명(authorNickname)을 함께 내려
 * 프론트가 별도 조회 없이 렌더링할 수 있게 한다.
 */
public record NoticeResponse(
        Long id,
        Long authorId,
        String authorNickname,
        String title,
        String content,
        boolean pinned,
        Integer viewCount,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static NoticeResponse from(Notice notice) {
        return new NoticeResponse(
                notice.getId(),
                notice.getAuthorId(),
                notice.getAuthorNickname(),
                notice.getTitle(),
                notice.getContent(),
                notice.isPinned(),
                notice.getViewCount(),
                notice.getCreatedAt(),
                notice.getUpdatedAt()
        );
    }
}
