package com.tripcrew.tripplan.model.mapper;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.tripcrew.tripplan.model.TripMemberRole;
import com.tripcrew.tripplan.model.dto.TripMember;
import com.tripcrew.tripplan.model.dto.TripMemberRow;

/**
 * trip_members(F06 공동편집 참여자) 매퍼. 소유자(OWNER)는 trip_plans.owner_id 로 관리하므로
 * 이 테이블에는 협업자(EDITOR/VIEWER)만 저장된다.
 */
@Mapper
public interface TripMemberMapper {

    /** 협업자 추가. (trip_plan_id, user_id) 유니크 위반 시 DuplicateKeyException. */
    int insert(TripMember member);

    /** 특정 계획의 특정 사용자 멤버십(권한 판정용). */
    Optional<TripMember> findByPlanAndUser(@Param("planId") Long planId,
                                           @Param("userId") Long userId);

    /** 특정 계획의 협업자 목록(이메일/닉네임 조인, 최근 추가순). 소유자는 서비스에서 합성해 추가한다. */
    List<TripMemberRow> findByPlan(@Param("planId") Long planId);

    /** 내가 협업자로 참여 중인 멤버십 목록(공유받은 계획 조회용). */
    List<TripMember> findByUserId(@Param("userId") Long userId);

    /** 역할 변경. affected 0 이면 대상 멤버 없음. */
    int updateRole(@Param("planId") Long planId,
                   @Param("userId") Long userId,
                   @Param("role") TripMemberRole role);

    /** 협업자 제거. affected 0 이면 대상 멤버 없음. */
    int delete(@Param("planId") Long planId,
               @Param("userId") Long userId);
}
