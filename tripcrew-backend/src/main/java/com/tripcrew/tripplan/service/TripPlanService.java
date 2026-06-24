package com.tripcrew.tripplan.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tripcrew.activity.service.UserActivityService;
import com.tripcrew.common.exception.BusinessException;
import com.tripcrew.tripplan.exception.OptimisticLockConflictException;
import com.tripcrew.tripplan.exception.TripPlanNotFoundException;
import com.tripcrew.tripplan.model.TripMemberRole;
import com.tripcrew.tripplan.model.dto.TripMember;
import com.tripcrew.tripplan.model.dto.TripPlan;
import com.tripcrew.tripplan.model.dto.TripPlanCreateRequest;
import com.tripcrew.tripplan.model.dto.TripPlanResponse;
import com.tripcrew.tripplan.model.dto.TripPlanUpdateRequest;
import com.tripcrew.tripplan.model.mapper.TripMemberMapper;
import com.tripcrew.tripplan.model.mapper.TripPlanMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TripPlanService {

    private final TripPlanMapper tripPlanMapper;
    private final TripMemberMapper tripMemberMapper;
    private final TripPlanAccessService accessService;
    private final UserActivityService userActivityService;

    /** 여행계획 생성. 소유자는 인증 주체. DB DEFAULT 가 채운 값까지 반영해 재조회 반환. */
    @Transactional
    public TripPlanResponse create(Long ownerId, TripPlanCreateRequest request) {
        validateDateRange(request.startDate(), request.endDate());

        TripPlan plan = TripPlan.builder()
                .ownerId(ownerId)
                .title(request.title())
                .description(request.description())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .build();
        tripPlanMapper.insert(plan);
        userActivityService.record(ownerId, "PLAN_CREATED", plan.getId(), plan.getTitle(), null, null);

        return TripPlanResponse.from(findOrThrow(plan.getId()));
    }

    /**
     * 단건 조회. 인증된 사용자라면 누구나 조회 가능(공유 열람).
     * 응답의 myRole 로 요청자의 역할(OWNER/EDITOR/VIEWER, 멤버 아니면 null)을 함께 내려
     * 프론트가 편집 UI 노출 여부를 정하게 한다.
     */
    @Transactional(readOnly = true)
    public TripPlanResponse get(Long id, Long userId) {
        TripPlan plan = findOrThrow(id);
        return TripPlanResponse.from(plan, accessService.resolveRole(plan, userId));
    }

    /**
     * 내가 접근 가능한 여행계획 목록(최신순). 내가 소유한 계획(OWNER)과
     * 협업자로 초대받아 공유받은 계획(EDITOR/VIEWER)을 합쳐서 반환한다.
     */
    @Transactional(readOnly = true)
    public List<TripPlanResponse> listMine(Long userId) {
        List<TripPlanResponse> result = new ArrayList<>();
        for (TripPlan plan : tripPlanMapper.findByOwnerId(userId)) {
            result.add(TripPlanResponse.from(plan, TripMemberRole.OWNER));
        }
        // 공유받은 계획. 한 사용자가 협업 중인 계획 수는 적어 N+1 영향이 미미하다.
        for (TripMember membership : tripMemberMapper.findByUserId(userId)) {
            tripPlanMapper.findById(membership.getTripPlanId())
                    .ifPresent(plan -> result.add(TripPlanResponse.from(plan, membership.getRole())));
        }
        result.sort(Comparator.comparing(TripPlanResponse::updatedAt,
                Comparator.nullsLast(Comparator.reverseOrder())));
        return result;
    }

    /**
     * 여행계획 수정. 소유자 또는 EDITOR 가능하며 낙관적 락으로 동시 수정을 막는다.
     * version 불일치 시 UPDATE 가 0행을 갱신 → 409 충돌.
     */
    @Transactional
    public TripPlanResponse update(Long id, Long userId, TripPlanUpdateRequest request) {
        accessService.requireEditor(id, userId);
        validateDateRange(request.startDate(), request.endDate());

        TripPlan changes = TripPlan.builder()
                .title(request.title())
                .description(request.description())
                .startDate(request.startDate())
                .endDate(request.endDate())
                .build();
        int affected = tripPlanMapper.updateWithVersion(id, request.version(), changes);
        if (affected == 0) {
            throw new OptimisticLockConflictException();
        }

        // 내 역할을 함께 내려준다(미포함이면 프론트에서 myRole=null → 편집 UI 가 사라짐).
        TripPlan updated = findOrThrow(id);
        return TripPlanResponse.from(updated, accessService.resolveRole(updated, userId));
    }

    /** 여행계획 삭제. 소유자만 가능. */
    @Transactional
    public void delete(Long id, Long userId) {
        accessService.requireOwner(id, userId);
        tripPlanMapper.deleteById(id);
    }

    private TripPlan findOrThrow(Long id) {
        return tripPlanMapper.findById(id)
                .orElseThrow(TripPlanNotFoundException::new);
    }

    private void validateDateRange(java.time.LocalDate start, java.time.LocalDate end) {
        if (start != null && end != null && end.isBefore(start)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "종료일은 시작일보다 빠를 수 없습니다.");
        }
    }
}
