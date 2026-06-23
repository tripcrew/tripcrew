package com.tripcrew.like.dto;

/**
 * 관광지 좋아요 상태 응답.
 *
 * @param liked     현재 사용자가 좋아요했는지(비로그인이면 false)
 * @param likeCount 총 좋아요 수
 */
public record LikeStatusResponse(boolean liked, long likeCount) {
}
