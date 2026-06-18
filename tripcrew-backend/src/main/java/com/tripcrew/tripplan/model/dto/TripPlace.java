package com.tripcrew.tripplan.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * trip_places 테이블 매핑. 계획에 담긴 장소이며 visitDay 가 null 이면
 * 아직 일정 Day 에 배치되지 않은 후보 장소로 취급한다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripPlace {

    private Long id;
    private Long tripPlanId;
    private Integer attractionId;
    private String name;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Integer visitDay;
    private Integer orderIndex;
    private String memo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
