package com.tripcrew.tripplan.model.dto;

import java.time.LocalDateTime;

import com.tripcrew.tripplan.model.TripMemberRole;
import com.tripcrew.tripplan.model.TripMemberStatus;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 멤버 목록 조회용 조인 결과(trip_members ⨝ users). 사람은 이메일/닉네임으로 식별해 내려준다.
 * MyBatis 가 setter 로 채우므로 Lombok 가변 클래스로 둔다. → {@link TripMemberResponse#from} 으로 변환.
 */
@Getter
@Setter
@NoArgsConstructor
public class TripMemberRow {

    private Long userId;
    private String email;
    private String nickname;
    private TripMemberRole role;
    private TripMemberStatus status;
    private LocalDateTime createdAt;
}
