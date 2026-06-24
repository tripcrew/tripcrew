package com.tripcrew.restriction.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tripcrew.auth.model.mapper.RefreshTokenMapper;
import com.tripcrew.notification.model.NotificationType;
import com.tripcrew.notification.service.NotificationService;
import com.tripcrew.restriction.model.RestrictionType;
import com.tripcrew.user.model.Role;
import com.tripcrew.user.model.Status;
import com.tripcrew.user.model.dto.User;
import com.tripcrew.user.model.mapper.UserMapper;

import lombok.RequiredArgsConstructor;

/**
 * 로드맵 ④ 신고 단계별 제재(graduated sanctions)의 사다리 적용 권한자.
 * 신고 처리완료로 누적 횟수가 임계에 도달하면 약→강으로 단계 제재를 적용한다.
 *
 * <ul>
 *   <li>3회 → 후기 작성 {@value #RESTRICTION_DAYS}일 금지(REVIEW_WRITE)</li>
 *   <li>5회 → 여행계획 작성 {@value #RESTRICTION_DAYS}일 금지(PLAN_CREATE)</li>
 *   <li>10회 → 계정 {@value #RESTRICTION_DAYS}일 임시정지(ACCOUNT_SUSPEND, 세션 폐기)</li>
 *   <li>15회 → 영구정지는 <b>자동이 아니라</b> 관리자 수동 확정 — 시스템은 검토 알림만 보낸다
 *       (오신고 누적에 의한 자동 영구밴 방지).</li>
 * </ul>
 *
 * <p>대상은 일반 USER 한정 — ADMIN/SUPER_ADMIN 은 자동 제재하지 않고, 이미 영구정지(BANNED)면 무시한다.
 * 누적은 +1 씩 증가하므로 각 임계를 정확히 한 번씩 통과한다(case 매칭).
 */
@Service
@RequiredArgsConstructor
public class SanctionService {

    static final int THRESHOLD_REVIEW_WRITE = 3;
    static final int THRESHOLD_PLAN_CREATE = 5;
    static final int THRESHOLD_ACCOUNT_SUSPEND = 10;
    static final int THRESHOLD_PERMANENT_REVIEW = 15;
    static final int RESTRICTION_DAYS = 7;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final RestrictionService restrictionService;
    private final NotificationService notificationService;
    private final RefreshTokenMapper refreshTokenMapper;
    private final UserMapper userMapper;

    /**
     * 누적 신고 횟수에 따라 단계 제재를 적용한다(신고 처리 트랜잭션 안에서 호출).
     *
     * @param count 이번 +1 이 반영된 누적 신고 횟수
     * @return 이번 호출로 능력제재/계정정지가 적용됐으면 true (영구정지 검토 알림만 보낸 경우는 false)
     */
    @Transactional
    public boolean applyForReportCount(Long userId, int count) {
        User user = userMapper.findById(userId).orElse(null);
        if (user == null || user.getRole() != Role.USER || user.getStatus() == Status.BANNED) {
            return false; // 관리자/이미 영구정지 대상은 자동 단계 제재 제외
        }

        LocalDateTime until = LocalDateTime.now().plusDays(RESTRICTION_DAYS);
        return switch (count) {
            case THRESHOLD_REVIEW_WRITE -> {
                restrictionService.restrict(userId, RestrictionType.REVIEW_WRITE, until, reason(count));
                notifyUser(userId, "신고 누적으로 후기 작성이 제한됩니다.", until);
                yield true;
            }
            case THRESHOLD_PLAN_CREATE -> {
                restrictionService.restrict(userId, RestrictionType.PLAN_CREATE, until, reason(count));
                notifyUser(userId, "신고 누적으로 여행계획 작성이 제한됩니다.", until);
                yield true;
            }
            case THRESHOLD_ACCOUNT_SUSPEND -> {
                restrictionService.restrict(userId, RestrictionType.ACCOUNT_SUSPEND, until, reason(count));
                refreshTokenMapper.deleteByUserId(userId); // 재발급 차단(기발급 access 는 만료 ≤30분 후 무효)
                notifyUser(userId, "신고 누적으로 계정이 일시 정지됩니다.", until);
                yield true;
            }
            case THRESHOLD_PERMANENT_REVIEW -> {
                flagForManualReview(userId, count); // 영구정지는 관리자 수동 확정 — 자동 밴 없음
                yield false;
            }
            default -> false;
        };
    }

    private void notifyUser(Long userId, String what, LocalDateTime until) {
        String msg = what + " (해제: " + until.format(FMT) + ") 이의가 있으면 1:1 문의로 알려주세요.";
        notificationService.notify(userId, NotificationType.SANCTION_APPLIED, null, msg);
    }

    /** 최고 임계 도달 — 관리자 전원에게 영구정지 검토 알림(대상 사용자 id 를 ref_id 로). */
    private void flagForManualReview(Long userId, int count) {
        String msg = String.format("신고 누적 %d회 사용자가 있습니다. 영구 정지가 필요한지 검토해 주세요.", count);
        for (Long adminId : userMapper.findAdminIds()) {
            notificationService.notify(adminId, NotificationType.SANCTION_REVIEW_REQUIRED, userId, msg);
        }
    }

    private String reason(int count) {
        return String.format("신고 누적 %d회 자동 단계 제재", count);
    }
}
