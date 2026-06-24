package com.tripcrew.like.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 관광지 카드용 배치 좋아요 통계. 검색/목록 결과의 여러 관광지에 대해
 * 총 찜 수(likeCount)와 현재 사용자의 찜 여부(liked)를 한 번에 내린다(N+1 방지).
 * 비로그인 사용자는 liked=false.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AttractionLikeCount {
    private Integer no;
    private Long likeCount;
    private boolean liked;
}
