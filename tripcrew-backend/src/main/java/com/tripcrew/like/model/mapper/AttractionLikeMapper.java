package com.tripcrew.like.model.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.tripcrew.like.dto.AttractionLikeCount;
import com.tripcrew.like.dto.WishlistItemResponse;

@Mapper
public interface AttractionLikeMapper {

    /** 좋아요 추가(멱등). 이미 있으면 무시(INSERT IGNORE). @return 새로 추가됐으면 1, 이미 있었으면 0. */
    int insertIgnore(@Param("userId") Long userId, @Param("attractionNo") Integer attractionNo);

    /** 좋아요 취소. @return 삭제된 행 수(없었으면 0). */
    int delete(@Param("userId") Long userId, @Param("attractionNo") Integer attractionNo);

    /** 해당 사용자가 이 관광지를 좋아요했는지. */
    boolean exists(@Param("userId") Long userId, @Param("attractionNo") Integer attractionNo);

    /** 관광지의 총 좋아요 수. */
    long countByAttraction(@Param("attractionNo") Integer attractionNo);

    /** 대상 관광지 존재 여부(앱레벨 검증 — 없으면 404). */
    boolean existsAttraction(@Param("attractionNo") Integer attractionNo);

    /** 내가 찜한 관광지 목록(카드 정보 + 평점 요약 + 총 찜 수 + 찜한 시각). 최근 찜 순. */
    List<WishlistItemResponse> findLikedByUser(@Param("userId") Long userId);

    /**
     * 여러 관광지의 총 찜 수 + 현재 사용자 찜 여부를 한 번에 조회(N+1 방지).
     * 찜이 0건인 관광지는 결과에 없을 수 있다(호출측에서 0/false 기본값 처리).
     * userId 가 null(비로그인)이면 liked 는 모두 false.
     */
    List<AttractionLikeCount> findLikeCountsByNos(@Param("userId") Long userId,
                                                  @Param("nos") List<Integer> nos);
}
