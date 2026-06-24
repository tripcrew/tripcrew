package com.tripcrew.tripplan.model.dto;

import java.time.LocalDateTime;

import com.tripcrew.tripplan.model.TripMemberRole;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 내가 받은(수락 대기 중) 초대 한 건. trip_members ⨝ trip_plans ⨝ users(소유자) 조인 결과.
 * MyBatis 가 setter 로 채우므로 가변 클래스로 둔다(프로젝트의 다른 조회 DTO 와 동일).
 *
 * <p>{@code role} 은 초대받은 역할(EDITOR/VIEWER), {@code inviterNickname} 은 초대한 소유자.
 */
@Getter
@Setter
@NoArgsConstructor
public class InviteResponse {

    private Long planId;
    private String planTitle;
    private String inviterNickname;
    private TripMemberRole role;
    private LocalDateTime invitedAt;
}
