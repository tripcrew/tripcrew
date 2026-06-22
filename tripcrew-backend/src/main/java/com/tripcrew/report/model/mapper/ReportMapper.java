package com.tripcrew.report.model.mapper;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.tripcrew.report.model.ReportStatus;
import com.tripcrew.report.model.ReportTargetType;
import com.tripcrew.report.model.dto.AdminReportRow;
import com.tripcrew.report.model.dto.Report;

@Mapper
public interface ReportMapper {

    /** 성공 시 report.id 에 생성된 PK가 채워진다(useGeneratedKeys). status 는 DB DEFAULT 'OPEN'. */
    int insert(Report report);

    Optional<Report> findById(Long id);

    /**
     * 신고가 가리키는 "피신고 유저"(제재 대상)의 id. REVIEW=후기 작성자, USER=대상 사용자.
     * 후기가 이미 삭제됐으면 null 일 수 있다.
     */
    Long findReportedUserId(@Param("id") Long id);

    /** 같은 신고자가 같은 대상을 이미 신고했는지(중복 신고 방지 사전 체크). */
    boolean existsByReporterAndTarget(@Param("reporterId") Long reporterId,
                                      @Param("targetType") ReportTargetType targetType,
                                      @Param("targetId") Long targetId);

    /** 후기(reviews.id) 존재 여부 — 대상 검증용(앱레벨). */
    boolean existsReview(@Param("targetId") Long targetId);

    /** 사용자(users.id) 존재 여부 — 대상 검증용(앱레벨). */
    boolean existsUser(@Param("targetId") Long targetId);

    /**
     * 관리자 신고 목록(최신순). 신고자/피신고 후기 내용/피신고 유저/누적 신고 횟수를 조인·집계해 내려준다.
     * status 가 null 이면 전체, 아니면 해당 상태만.
     */
    List<AdminReportRow> findAllForAdmin(@Param("status") ReportStatus status);

    /** 신고 처리상태 변경. affected rows 0 이면 대상 신고 없음. */
    int updateStatus(@Param("id") Long id, @Param("status") ReportStatus status);
}
