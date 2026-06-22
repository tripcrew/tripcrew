package com.tripcrew.report.model.dto;

import java.time.LocalDateTime;

import com.tripcrew.report.model.ReportReason;
import com.tripcrew.report.model.ReportStatus;
import com.tripcrew.report.model.ReportTargetType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * reports 테이블 매핑. (MyBatis 가 setter 로 채우므로 기본생성자/세터 필요)
 * status / created_at / updated_at 은 DB DEFAULT 로 채워지므로 INSERT 시 다루지 않는다.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report {

    private Long id;
    private Long reporterId;
    private ReportTargetType targetType;
    private Long targetId;
    private ReportReason reason;
    private String detail;
    private ReportStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
