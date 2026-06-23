package com.tripcrew.review.model.dto;

import lombok.Getter;
import lombok.Setter;

/** 별점 분포 집계 한 행(rating 점 × count 개). 목록 요약의 distribution 을 만드는 데 쓴다. */
@Getter
@Setter
public class RatingCount {
    private int rating;
    private long count;
}
