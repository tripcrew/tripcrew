package com.tripcrew.admin.model.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.tripcrew.restriction.model.dto.UserRestriction;
import com.tripcrew.user.model.dto.User;

/**
 * 관리자 화면용 사용자 응답. password 는 절대 포함하지 않는다.
 * activeRestrictions 로 신고 누적 단계 제재(후기/계획 금지·계정 임시정지) 현황을 함께 내려
 * 관리자가 영구정지(수동 확정) 여부를 판단할 수 있게 한다.
 */
public record AdminUserResponse(
        Long id,
        String email,
        String nickname,
        String role,
        String status,
        Integer reportCount,
        LocalDateTime createdAt,
        List<RestrictionView> activeRestrictions
) {
    /** 활성 제재 한 건(종류 + 해제 시각). until 이 null 이면 영구. */
    public record RestrictionView(String type, LocalDateTime until) {
        static RestrictionView from(UserRestriction r) {
            return new RestrictionView(r.getType().name(), r.getUntil());
        }
    }

    public static AdminUserResponse from(User user, List<UserRestriction> restrictions) {
        return new AdminUserResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getRole().name(),
                user.getStatus().name(),
                user.getReportCount(),
                user.getCreatedAt(),
                restrictions.stream().map(RestrictionView::from).toList()
        );
    }
}
