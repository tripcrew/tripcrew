package com.tripcrew.tripplan.model.dto;

import java.time.LocalDateTime;

import com.tripcrew.tripplan.model.TripMemberRole;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * trip_members 테이블 매핑(F06 공동편집 참여자). 소유자(OWNER)는 trip_plans.owner_id 로
 * 관리하므로 이 테이블에는 보통 협업자(EDITOR/VIEWER)만 저장된다.
 * created_at / updated_at 은 DB DEFAULT 로 채워지므로 INSERT 시 다루지 않는다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripMember {

    private Long id;
    private Long tripPlanId;
    private Long userId;
    private TripMemberRole role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
