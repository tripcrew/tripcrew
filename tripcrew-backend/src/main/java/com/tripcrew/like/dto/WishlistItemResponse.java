package com.tripcrew.like.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 내 찜 목록 아이템. 관광지 카드(AttractionSummaryResponse) 형태에
 * 평점 요약(avgRating/reviewCount)·총 찜 수(likeCount)·찜한 시각(likedAt)을 더해
 * 프론트가 카드 + 평점 + 계획에 담기까지 한 번에 그릴 수 있게 한다.
 *
 * <p>(MyBatis 가 setter 로 채우므로 클래스+세터로 둔다 — 프로젝트의 다른 조회 DTO 와 동일.)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WishlistItemResponse {

    private Integer no;
    private String title;
    private String imageUrl;
    private String sido;
    private String gugun;
    private String contentType;
    private String address;
    private Double avgRating;
    private Long reviewCount;
    private Long likeCount;
    private LocalDateTime likedAt;
}
