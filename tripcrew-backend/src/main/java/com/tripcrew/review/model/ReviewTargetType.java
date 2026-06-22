package com.tripcrew.review.model;

/**
 * 후기 대상 종류(폴리모픽). DB에는 VARCHAR로 이름(name)이 그대로 저장된다.
 * target_id 가 가리키는 테이블이 종류별로 달라 DB FK 제약을 둘 수 없으므로
 * 대상 존재 검증은 앱레벨(ReviewService)에서 한다.
 */
public enum ReviewTargetType {
    /** 관광지(attractions.no) */
    ATTRACTION,
    /** 여행계획(trip_plans.id) */
    TRIP_PLAN
}
