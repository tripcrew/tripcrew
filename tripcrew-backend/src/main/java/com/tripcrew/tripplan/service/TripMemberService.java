package com.tripcrew.tripplan.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tripcrew.common.exception.BusinessException;
import com.tripcrew.tripplan.exception.TripMemberNotFoundException;
import com.tripcrew.tripplan.model.TripMemberRole;
import com.tripcrew.tripplan.model.dto.TripMember;
import com.tripcrew.tripplan.model.dto.TripMemberResponse;
import com.tripcrew.tripplan.model.dto.TripMemberRow;
import com.tripcrew.tripplan.model.dto.TripPlan;
import com.tripcrew.tripplan.model.mapper.TripMemberMapper;
import com.tripcrew.user.model.dto.User;
import com.tripcrew.user.model.mapper.UserMapper;

import lombok.RequiredArgsConstructor;

/**
 * F06 공동편집 — 여행계획 멤버(협업자) 관리. 초대/역할변경/제거는 소유자(OWNER)만,
 * 목록 조회는 모든 멤버가 가능하다. 권한 판정은 {@link TripPlanAccessService} 에 위임한다.
 *
 * <p>소유권은 trip_plans.owner_id 가 단일 진실이므로 OWNER 는 trip_members 에 저장하지 않고
 * 목록 조회 시 가상 행으로 합성해 함께 내려준다.
 */
@Service
@RequiredArgsConstructor
public class TripMemberService {

    private final TripPlanAccessService accessService;
    private final TripMemberMapper tripMemberMapper;
    private final UserMapper userMapper;

    /** 멤버 목록(소유자 + 협업자). 모든 멤버가 조회 가능. */
    @Transactional(readOnly = true)
    public List<TripMemberResponse> list(Long planId, Long userId) {
        TripPlan plan = accessService.requireMember(planId, userId);

        List<TripMemberResponse> result = new ArrayList<>();
        result.add(ownerRow(plan));   // 소유자를 맨 위에 합성
        for (TripMemberRow row : tripMemberMapper.findByPlan(planId)) {
            result.add(TripMemberResponse.from(row));
        }
        return result;
    }

    /** 이메일로 협업자 초대(소유자만). role 은 EDITOR/VIEWER 만. */
    @Transactional
    public TripMemberResponse invite(Long planId, Long ownerId, String email, TripMemberRole role) {
        TripPlan plan = accessService.requireOwner(planId, ownerId);
        if (role == TripMemberRole.OWNER) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "OWNER 로는 초대할 수 없습니다. (소유권 이전은 지원하지 않습니다)");
        }

        User invitee = userMapper.findByEmail(email)
                .orElseThrow(() -> new BusinessException(HttpStatus.NOT_FOUND, "해당 이메일의 사용자를 찾을 수 없습니다."));
        if (invitee.getId().equals(plan.getOwnerId())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "소유자는 멤버로 초대할 수 없습니다.");
        }
        if (tripMemberMapper.findByPlanAndUser(planId, invitee.getId()).isPresent()) {
            throw new BusinessException(HttpStatus.CONFLICT, "이미 참여 중인 멤버입니다.");
        }

        TripMember member = TripMember.builder()
                .tripPlanId(planId)
                .userId(invitee.getId())
                .role(role)
                .build();
        tripMemberMapper.insert(member);

        return new TripMemberResponse(invitee.getId(), invitee.getEmail(), invitee.getNickname(),
                role, null);
    }

    /** 협업자 역할 변경(소유자만). EDITOR ↔ VIEWER. */
    @Transactional
    public void updateRole(Long planId, Long ownerId, Long targetUserId, TripMemberRole role) {
        accessService.requireOwner(planId, ownerId);
        if (role == TripMemberRole.OWNER) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "OWNER 역할은 부여할 수 없습니다.");
        }
        int affected = tripMemberMapper.updateRole(planId, targetUserId, role);
        if (affected == 0) {
            throw new TripMemberNotFoundException();
        }
    }

    /**
     * 멤버 제거. 소유자는 누구든 내보낼 수 있고, 협업자는 본인만 탈퇴할 수 있다.
     * 소유자 자신은 멤버 목록에 없으므로 제거 대상이 될 수 없다(계획 삭제로 처리).
     */
    @Transactional
    public void remove(Long planId, Long userId, Long targetUserId) {
        TripPlan plan = accessService.requireMember(planId, userId);

        boolean selfLeave = userId.equals(targetUserId);
        if (!selfLeave) {
            // 타인 제거는 소유자만
            if (!plan.getOwnerId().equals(userId)) {
                throw new com.tripcrew.tripplan.exception.TripPlanAccessDeniedException();
            }
        } else if (plan.getOwnerId().equals(userId)) {
            // 소유자 자가 탈퇴 불가
            throw new BusinessException(HttpStatus.BAD_REQUEST, "소유자는 탈퇴할 수 없습니다. 계획을 삭제하세요.");
        }

        int affected = tripMemberMapper.delete(planId, targetUserId);
        if (affected == 0) {
            throw new TripMemberNotFoundException();
        }
    }

    /** 소유자 가상 멤버 행(목록 상단). */
    private TripMemberResponse ownerRow(TripPlan plan) {
        User owner = userMapper.findById(plan.getOwnerId())
                .orElseThrow(() -> new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "소유자 정보를 찾을 수 없습니다."));
        return new TripMemberResponse(owner.getId(), owner.getEmail(), owner.getNickname(),
                TripMemberRole.OWNER, plan.getCreatedAt());
    }
}
