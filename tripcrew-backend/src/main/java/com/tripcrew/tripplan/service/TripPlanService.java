package com.tripcrew.tripplan.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tripcrew.common.exception.BusinessException;
import com.tripcrew.tripplan.exception.OptimisticLockConflictException;
import com.tripcrew.tripplan.exception.TripPlanAccessDeniedException;
import com.tripcrew.tripplan.exception.TripPlanNotFoundException;
import com.tripcrew.tripplan.model.dto.TripPlan;
import com.tripcrew.tripplan.model.dto.TripPlanCreateRequest;
import com.tripcrew.tripplan.model.dto.TripPlanResponse;
import com.tripcrew.tripplan.model.dto.TripPlanUpdateRequest;
import com.tripcrew.tripplan.model.mapper.TripPlanMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TripPlanService {

    private final TripPlanMapper tripPlanMapper;

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

        return TripPlanResponse.from(findOrThrow(plan.getId()));
    }

    /** 단건 조회. 인증된 사용자라면 누구나 조회 가능(공유 열람). */
    @Transactional(readOnly = true)
    public TripPlanResponse get(Long id) {
        return TripPlanResponse.from(findOrThrow(id));
    }

    /** 내 여행계획 목록(최신순). */
    @Transactional(readOnly = true)
    public List<TripPlanResponse> listMine(Long ownerId) {
        return tripPlanMapper.findByOwnerId(ownerId).stream()
                .map(TripPlanResponse::from)
                .toList();
    }

    /**
     * 여행계획 수정. 소유자만 가능하며 낙관적 락으로 동시 수정을 막는다.
     * version 불일치 시 UPDATE 가 0행을 갱신 → 409 충돌.
     */
    @Transactional
    public TripPlanResponse update(Long id, Long userId, TripPlanUpdateRequest request) {
        TripPlan existing = findOrThrow(id);
        if (!existing.getOwnerId().equals(userId)) {
            throw new TripPlanAccessDeniedException();
        }
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

        return TripPlanResponse.from(findOrThrow(id));
    }

    /** 여행계획 삭제. 소유자만 가능. */
    @Transactional
    public void delete(Long id, Long userId) {
        TripPlan existing = findOrThrow(id);
        if (!existing.getOwnerId().equals(userId)) {
            throw new TripPlanAccessDeniedException();
        }
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
