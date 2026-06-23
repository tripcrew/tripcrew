package com.tripcrew.report.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tripcrew.common.exception.BusinessException;
import com.tripcrew.report.model.ReportTargetType;
import com.tripcrew.report.model.dto.Report;
import com.tripcrew.report.model.dto.ReportCreateRequest;
import com.tripcrew.report.model.mapper.ReportMapper;
import com.tripcrew.review.model.dto.Review;
import com.tripcrew.review.model.mapper.ReviewMapper;

import lombok.RequiredArgsConstructor;

/**
 * F09 Phase 2: 신고 생성(사용자). 폴리모픽 대상(REVIEW | USER)에 대한 신고를 접수한다.
 * 대상 테이블이 종류별로 달라 DB FK 를 둘 수 없으므로 대상 존재 검증은 여기(앱레벨)에서 한다.
 */
@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReportMapper reportMapper;
    private final ReviewMapper reviewMapper;

    /**
     * 신고 접수. 신고자는 인증 주체.
     * <ul>
     *   <li>대상 존재 검증(없으면 400)</li>
     *   <li>본인(또는 본인 후기) 신고 차단(400)</li>
     *   <li>같은 대상 중복 신고 차단(409)</li>
     * </ul>
     */
    @Transactional
    public void create(Long reporterId, ReportCreateRequest request) {
        validateTargetAndSelf(reporterId, request.targetType(), request.targetId());

        if (reportMapper.existsByReporterAndTarget(reporterId, request.targetType(), request.targetId())) {
            throw new BusinessException(HttpStatus.CONFLICT, "이미 신고한 대상입니다.");
        }

        Report report = Report.builder()
                .reporterId(reporterId)
                .targetType(request.targetType())
                .targetId(request.targetId())
                .reason(request.reason())
                .detail(request.detail())
                .build();
        reportMapper.insert(report);
    }

    private void validateTargetAndSelf(Long reporterId, ReportTargetType targetType, Long targetId) {
        switch (targetType) {
            case REVIEW -> {
                Review review = reviewMapper.findById(targetId)
                        .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, "신고 대상 후기가 존재하지 않습니다."));
                if (review.getUserId().equals(reporterId)) {
                    throw new BusinessException(HttpStatus.BAD_REQUEST, "본인이 작성한 후기는 신고할 수 없습니다.");
                }
            }
            case USER -> {
                if (targetId.equals(reporterId)) {
                    throw new BusinessException(HttpStatus.BAD_REQUEST, "본인은 신고할 수 없습니다.");
                }
                if (!reportMapper.existsUser(targetId)) {
                    throw new BusinessException(HttpStatus.BAD_REQUEST, "신고 대상 사용자가 존재하지 않습니다.");
                }
            }
        }
    }
}
