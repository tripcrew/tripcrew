package com.tripcrew.report.model.dto;

import java.time.LocalDateTime;

import com.tripcrew.report.model.ReportReason;
import com.tripcrew.report.model.ReportTargetType;

import lombok.Getter;
import lombok.Setter;

/**
 * 관리자 신고 목록 조회용 조인 프로젝션(읽기 전용). MyBatis 가 setter 로 채운다.
 * 사람(신고자/피신고 유저)은 식별이 명확한 이메일로 내려준다. (PK 노출 대신)
 *
 * <p>"피신고 유저"는 제재(밴) 대상이 되는 사람으로, REVIEW 신고는 그 후기의 작성자,
 * USER 신고는 대상 사용자 자신이다.
 */
@Getter
@Setter
public class AdminReportRow {

    private Long id;
    private LocalDateTime createdAt;
    private ReportTargetType targetType;   // REVIEW | USER (무엇이 신고됐는지)
    private ReportReason reason;
    private String detail;

    private String reporterEmail;          // 신고자
    private String reviewContent;          // REVIEW 신고일 때만 채워짐
    private String reportedUserEmail;      // 피신고 유저(밴 대상): REVIEW=후기 작성자, USER=대상 사용자
}
