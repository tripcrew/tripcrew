package com.tripcrew.review.model.mapper;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.tripcrew.review.model.ReviewTargetType;
import com.tripcrew.review.model.dto.Review;

@Mapper
public interface ReviewMapper {

    /** 성공 시 review.id 에 생성된 PK가 채워진다(useGeneratedKeys). */
    int insert(Review review);

    Optional<Review> findById(Long id);

    /** 특정 대상(폴리모픽)의 후기 목록(최신순). 작성자 닉네임을 users 조인으로 채운다. */
    List<Review> findByTarget(@Param("targetType") ReviewTargetType targetType,
                              @Param("targetId") Long targetId);

    /** 대상 존재 검증용(앱레벨). 관광지(attractions.no) 존재 여부. */
    boolean existsAttraction(@Param("targetId") Long targetId);

    /** 대상 존재 검증용(앱레벨). 여행계획(trip_plans.id) 존재 여부. */
    boolean existsTripPlan(@Param("targetId") Long targetId);

    /**
     * 후기를 숨김(soft-delete)으로 전환. 신고 처리완료(RESOLVED) 시 호출.
     * 하드삭제가 아니라 status=HIDDEN 으로만 바꿔 row 를 보존한다.
     *
     * @return 영향받은 행 수(이미 없는 id 면 0)
     */
    int hideById(@Param("id") Long id);
}
