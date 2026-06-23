package com.tripcrew.review.model.mapper;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.tripcrew.review.model.ReviewTargetType;
import com.tripcrew.review.model.dto.RatingCount;
import com.tripcrew.review.model.dto.Review;
import com.tripcrew.review.model.dto.ReviewStats;

@Mapper
public interface ReviewMapper {

    /** 성공 시 review.id 에 생성된 PK가 채워진다(useGeneratedKeys). */
    int insert(Review review);

    Optional<Review> findById(Long id);

    /**
     * 특정 대상(폴리모픽)의 후기 목록(VISIBLE 만, 페이징+정렬). 작성자 닉네임을 users 조인으로 채운다.
     *
     * @param sort 정렬 키(서비스에서 화이트리스트 검증한 값): LATEST | RATING_HIGH | RATING_LOW
     */
    List<Review> findByTargetPaged(@Param("targetType") ReviewTargetType targetType,
                                   @Param("targetId") Long targetId,
                                   @Param("sort") String sort,
                                   @Param("offset") int offset,
                                   @Param("size") int size);

    /** 대상의 VISIBLE 후기 총개수(페이징 totalElements 용). */
    long countByTarget(@Param("targetType") ReviewTargetType targetType,
                       @Param("targetId") Long targetId);

    /** 대상의 비정규화 집계(없으면 empty). 목록 요약(평균/개수)에 쓴다. */
    Optional<ReviewStats> findStats(@Param("targetType") ReviewTargetType targetType,
                                    @Param("targetId") Long targetId);

    /** 대상의 별점 분포(VISIBLE, rating 별 개수). 비어 있을 수 있다(점수 없는 별은 0 으로 채움은 서비스에서). */
    List<RatingCount> findDistribution(@Param("targetType") ReviewTargetType targetType,
                                       @Param("targetId") Long targetId);

    /** 대상 존재 검증용(앱레벨). 관광지(attractions.no) 존재 여부. */
    boolean existsAttraction(@Param("targetId") Long targetId);

    /** 대상 존재 검증용(앱레벨). 여행계획(trip_plans.id) 존재 여부. */
    boolean existsTripPlan(@Param("targetId") Long targetId);

    /** 후기 평점/내용 수정(본인 검증은 서비스). @return 영향받은 행 수. */
    int update(@Param("id") Long id,
               @Param("rating") Integer rating,
               @Param("content") String content);

    /** 후기 하드삭제(본인 검증은 서비스). @return 영향받은 행 수. */
    int deleteById(@Param("id") Long id);

    /**
     * 후기를 숨김(soft-delete)으로 전환. 신고 처리완료(RESOLVED) 시 호출.
     * 하드삭제가 아니라 status=HIDDEN 으로만 바꿔 row 를 보존한다.
     *
     * @return 영향받은 행 수(이미 없는 id 면 0)
     */
    int hideById(@Param("id") Long id);

    /**
     * 대상 집계(review_stats)를 증분 갱신(upsert). 행이 없으면 생성, 있으면 델타를 더한다.
     * avg_rating 은 갱신 후 review_count/rating_sum 으로 재계산(count=0 이면 0).
     *
     * <p>create: (+1, +rating) / delete·hide(이전 VISIBLE): (-1, -rating) / update(VISIBLE): (0, new-old).
     */
    int applyStatsDelta(@Param("targetType") ReviewTargetType targetType,
                        @Param("targetId") Long targetId,
                        @Param("countDelta") int countDelta,
                        @Param("ratingDelta") long ratingDelta);
}
