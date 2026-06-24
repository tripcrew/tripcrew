package com.tripcrew.admin.model.dto;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

/**
 * 일자별 건수(가입/후기/신고 추이 차트용). MyBatis 가 {@code DATE(created_at)} 를 day 에 매핑한다.
 */
@Getter
@Setter
public class DailyCount {
    private LocalDate day;
    private long count;
}
