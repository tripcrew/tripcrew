package com.tripcrew.tripplan.model.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * trip_plans 테이블 매핑. (MyBatis 가 setter 로 채우므로 기본생성자/세터 필요)
 *
 * <p>created_at / updated_at / view_count / version 은 DB DEFAULT 로 채워지므로
 * INSERT 시 다루지 않는다. version 은 낙관적 락에 쓰이며 UPDATE 에서
 * {@code version = version + 1} 로 증가시킨다(MyBatis 라 자동증가 없음).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripPlan {

    private Long id;
    private Long ownerId;
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private Long viewCount;
    private Long version;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
