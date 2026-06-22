package com.tripcrew.notice.model.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * notices 테이블 매핑(F10 공지사항). (MyBatis 가 setter 로 채우므로 기본생성자/세터 필요)
 * view_count / created_at / updated_at 은 DB 가 관리하므로 INSERT 시 다루지 않는다.
 *
 * <p>{@code authorNickname} 은 notices 컬럼이 아니라 조회 시 users 조인으로 채우는 값이다
 * (작성 관리자 표시용). 작성자가 탈퇴하면 author_id 가 NULL 이라 비어 있을 수 있다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notice {

    private Long id;
    private Long authorId;
    private String title;
    private String content;
    private boolean pinned;
    private Integer viewCount;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    /** 조인으로 채우는 작성 관리자 닉네임(notices 컬럼 아님). 탈퇴 시 NULL. */
    private String authorNickname;
}
