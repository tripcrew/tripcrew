package com.tripcrew.tripplan.model.mapper;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.tripcrew.tripplan.model.dto.TripPlan;

@Mapper
public interface TripPlanMapper {

    /** 성공 시 plan.id 에 생성된 PK가 채워진다(useGeneratedKeys). */
    int insert(TripPlan plan);

    Optional<TripPlan> findById(Long id);

    /** 특정 사용자가 소유한 계획 목록(최신순). */
    List<TripPlan> findByOwnerId(Long ownerId);

    /**
     * 낙관적 락 UPDATE. {@code WHERE id = ? AND version = ?} 로 갱신하고
     * 성공 시 version 을 1 증가시킨다. 반환값(affected rows)이 0이면
     * version 불일치(동시 수정) 또는 행 없음 → 호출측에서 충돌로 처리.
     */
    int updateWithVersion(@Param("id") Long id,
                          @Param("expectedVersion") Long expectedVersion,
                          @Param("plan") TripPlan plan);

    int deleteById(Long id);
}
