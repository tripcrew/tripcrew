package com.tripcrew.tripplan.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tripcrew.tripplan.exception.TripPlanAccessDeniedException;
import com.tripcrew.tripplan.exception.TripPlanNotFoundException;
import com.tripcrew.tripplan.model.TripMemberRole;
import com.tripcrew.tripplan.model.TripMemberStatus;
import com.tripcrew.tripplan.model.dto.TripPlan;
import com.tripcrew.tripplan.model.mapper.TripMemberMapper;
import com.tripcrew.tripplan.model.mapper.TripPlanMapper;

import lombok.RequiredArgsConstructor;

/**
 * 여행계획 권한 판정(F06 공동편집). owner_id 가 OWNER 의 단일 진실이고,
 * 협업자(EDITOR/VIEWER)는 trip_members 로 관리한다.
 *
 * <pre>
 *   resolveRole : 소유자 → OWNER, 협업자 → 그 역할, 그 외 → null(접근 불가)
 *   requireMember : 모든 멤버(읽기) — 아니면 403
 *   requireEditor : OWNER/EDITOR(편집) — 아니면 403
 *   requireOwner  : OWNER(멤버 관리·삭제) — 아니면 403
 * </pre>
 *
 * 매퍼만 의존하므로 다른 서비스(TripPlan/TripPlace/TripMember)에서 순환 없이 재사용한다.
 */
@Service
@RequiredArgsConstructor
public class TripPlanAccessService {

    private final TripPlanMapper tripPlanMapper;
    private final TripMemberMapper tripMemberMapper;

    /** 사용자의 계획 내 역할. 멤버가 아니면 null. 계획이 없으면 404. */
    @Transactional(readOnly = true)
    public TripMemberRole resolveRole(Long planId, Long userId) {
        TripPlan plan = findPlanOrThrow(planId);
        return resolveRole(plan, userId);
    }

    /** 이미 조회한 plan 으로 역할 판정(중복 조회 회피). 수락(ACCEPTED)한 멤버만 권한 인정 — PENDING 은 접근 불가. */
    public TripMemberRole resolveRole(TripPlan plan, Long userId) {
        if (plan.getOwnerId().equals(userId)) {
            return TripMemberRole.OWNER;
        }
        return tripMemberMapper.findByPlanAndUser(plan.getId(), userId)
                .filter(m -> m.getStatus() == TripMemberStatus.ACCEPTED)
                .map(m -> m.getRole())
                .orElse(null);
    }

    /** 읽기 권한 보장. 멤버가 아니면 403. plan 반환. */
    public TripPlan requireMember(Long planId, Long userId) {
        TripPlan plan = findPlanOrThrow(planId);
        if (resolveRole(plan, userId) == null) {
            throw new TripPlanAccessDeniedException();
        }
        return plan;
    }

    /** 편집 권한 보장(OWNER/EDITOR). 아니면 403. plan 반환. */
    public TripPlan requireEditor(Long planId, Long userId) {
        TripPlan plan = findPlanOrThrow(planId);
        TripMemberRole role = resolveRole(plan, userId);
        if (role == null || !role.canEdit()) {
            throw new TripPlanAccessDeniedException();
        }
        return plan;
    }

    /** 소유자 권한 보장(멤버 관리·계획 삭제). 아니면 403. plan 반환. */
    public TripPlan requireOwner(Long planId, Long userId) {
        TripPlan plan = findPlanOrThrow(planId);
        if (!plan.getOwnerId().equals(userId)) {
            throw new TripPlanAccessDeniedException();
        }
        return plan;
    }

    private TripPlan findPlanOrThrow(Long planId) {
        return tripPlanMapper.findById(planId)
                .orElseThrow(TripPlanNotFoundException::new);
    }
}
