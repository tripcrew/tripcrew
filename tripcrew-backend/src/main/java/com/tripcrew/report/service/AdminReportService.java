package com.tripcrew.report.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tripcrew.admin.service.AdminUserService;
import com.tripcrew.common.exception.BusinessException;
import com.tripcrew.report.model.ReportStatus;
import com.tripcrew.report.model.ReportTargetType;
import com.tripcrew.report.model.dto.AdminReportResponse;
import com.tripcrew.report.model.dto.Report;
import com.tripcrew.report.model.mapper.ReportMapper;
import com.tripcrew.review.service.ReviewService;
import com.tripcrew.user.model.dto.User;
import com.tripcrew.user.model.mapper.UserMapper;

import lombok.RequiredArgsConstructor;

/**
 * F09 Phase 2: 관리자 신고 처리. 진입은 ROLE_ADMIN 으로 SecurityConfig(/api/admin/**)에서 막혀 있다.
 * 미처리(OPEN) 신고만 목록에 노출하고, 관리자가 처리완료/기각하면 목록에서 사라진다.
 *
 * <p><b>누적 자동 제재</b>: 신고를 처리완료(RESOLVED)할 때마다 피신고 유저의 누적 신고 횟수를 +1 하고,
 * {@link #SANCTION_THRESHOLD}회 이상이면 자동으로 제재한다(일반 USER 한정). 기각은 카운트를 늘리지 않는다.
 */
@Service
@RequiredArgsConstructor
public class AdminReportService {

    /** 신고 처리완료 누적이 이 횟수 이상이면 자동 제재한다. */
    private static final int SANCTION_THRESHOLD = 3;

    private final ReportMapper reportMapper;
    private final UserMapper userMapper;
    private final ReviewService reviewService;
    private final AdminUserService adminUserService;

    /** 신고 목록. status 가 null 이면 전체, 아니면 해당 상태만(보통 OPEN). */
    @Transactional(readOnly = true)
    public List<AdminReportResponse> list(ReportStatus status) {
        return reportMapper.findAllForAdmin(status).stream()
                .map(AdminReportResponse::from)
                .toList();
    }

    /**
     * 신고 처리완료(RESOLVED). 피신고 유저의 누적 신고 횟수를 +1 하고 임계치 이상이면 자동 제재.
     * 이미 OPEN 이 아니면(중복 처리) 카운트하지 않는다.
     *
     * @return 이번 처리로 자동 제재가 발동했으면 true
     */
    @Transactional
    public boolean resolve(Long reportId) {
        Report report = reportMapper.findById(reportId)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "신고를 찾을 수 없습니다."));
        if (report.getStatus() != ReportStatus.OPEN) {
            return false; // 이미 처리된 신고 — 중복 카운트 방지(멱등)
        }
        reportMapper.updateStatus(reportId, ReportStatus.RESOLVED);

        // 후기 신고를 처리완료하면 해당 후기를 숨김(soft-delete)으로 전환하고 평점 집계에서 제외.
        // 같은 트랜잭션이라 신고 처리·후기 숨김·집계 차감이 원자적으로 묶인다.
        // 하드삭제가 아니므로 아래 findReportedUserId 조인(작성자 식별)은 그대로 동작한다.
        if (report.getTargetType() == ReportTargetType.REVIEW) {
            reviewService.hideForReport(report.getTargetId());
        }

        Long reportedUserId = reportMapper.findReportedUserId(reportId);
        if (reportedUserId == null) {
            return false; // 대상 후기가 삭제된 경우 등 — 제재 대상 없음
        }
        userMapper.incrementReportCount(reportedUserId);

        User reported = userMapper.findById(reportedUserId).orElse(null);
        if (reported != null && reported.getReportCount() != null
                && reported.getReportCount() >= SANCTION_THRESHOLD) {
            return adminUserService.sanctionIfEligible(reportedUserId);
        }
        return false;
    }

    /** 신고 기각(DISMISSED). 신고 사유가 부적절한 경우. 누적 카운트는 늘리지 않는다. 대상 없으면 404. */
    @Transactional
    public void dismiss(Long reportId) {
        int affected = reportMapper.updateStatus(reportId, ReportStatus.DISMISSED);
        if (affected == 0) {
            throw new BusinessException(HttpStatus.NOT_FOUND, "신고를 찾을 수 없습니다.");
        }
    }
}
