package com.tripcrew.restriction.model.mapper;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.tripcrew.restriction.model.RestrictionType;
import com.tripcrew.restriction.model.dto.UserRestriction;

@Mapper
public interface UserRestrictionMapper {

    /** 제재 적재. created_at 은 DB DEFAULT. 생성된 id 는 dto 에 채워진다. */
    int insert(UserRestriction restriction);

    /**
     * 특정 대상+종류의 활성 제재(있으면). 활성 = until IS NULL(영구) 또는 until &gt; NOW().
     * 만료 시각 비교는 앱/DB 클럭 스큐를 피하려 DB의 NOW() 로 한다.
     * 영구 제재를 우선, 그다음 만료가 가장 늦은 것을 반환한다.
     */
    Optional<UserRestriction> findActiveByUserAndType(@Param("userId") Long userId,
                                                      @Param("type") RestrictionType type);

    /** 모든 활성 제재(관리자 목록에서 사용자별로 묶어 표시). 만료 행은 제외. */
    List<UserRestriction> findAllActive();
}
