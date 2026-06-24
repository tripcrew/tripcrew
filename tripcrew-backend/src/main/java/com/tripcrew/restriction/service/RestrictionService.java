package com.tripcrew.restriction.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tripcrew.restriction.exception.RestrictedActionException;
import com.tripcrew.restriction.model.RestrictionType;
import com.tripcrew.restriction.model.dto.UserRestriction;
import com.tripcrew.restriction.model.mapper.UserRestrictionMapper;

import lombok.RequiredArgsConstructor;

/**
 * 사용자 단계별 제재(graduated sanctions) 조회/적용. 신고 누적 임계에 따라
 * {@link com.tripcrew.restriction.service.SanctionService} 가 제재를 적재하고,
 * 후기/계획 작성 엔드포인트와 로그인 경로가 활성 제재를 확인해 차단한다.
 *
 * <p>"활성" 판단(만료 비교)은 DB의 NOW() 로 한다(앱/DB 클럭 스큐 회피).
 */
@Service
@RequiredArgsConstructor
public class RestrictionService {

    private final UserRestrictionMapper restrictionMapper;

    /** 특정 대상+종류의 활성 제재(있으면). 만료된 제재는 반환하지 않는다. */
    @Transactional(readOnly = true)
    public Optional<UserRestriction> activeRestriction(Long userId, RestrictionType type) {
        return restrictionMapper.findActiveByUserAndType(userId, type);
    }

    /** 활성 제재가 있으면 true. */
    @Transactional(readOnly = true)
    public boolean isRestricted(Long userId, RestrictionType type) {
        return activeRestriction(userId, type).isPresent();
    }

    /**
     * 해당 작업이 제재 중이면 403({@link RestrictedActionException})을 던진다.
     * 후기/계획 작성 POST 진입부에서 호출한다.
     */
    @Transactional(readOnly = true)
    public void requireAllowed(Long userId, RestrictionType type) {
        activeRestriction(userId, type).ifPresent(r -> {
            throw new RestrictedActionException(type, r.getUntil());
        });
    }

    /** 제재 적재. until 이 null 이면 영구. */
    @Transactional
    public void restrict(Long userId, RestrictionType type, LocalDateTime until, String reason) {
        restrictionMapper.insert(UserRestriction.builder()
                .userId(userId)
                .type(type)
                .until(until)
                .reason(reason)
                .build());
    }

    /** 모든 활성 제재를 사용자 id 로 묶어 반환(관리자 목록 표시용). */
    @Transactional(readOnly = true)
    public Map<Long, List<UserRestriction>> activeByUser() {
        return restrictionMapper.findAllActive().stream()
                .collect(Collectors.groupingBy(UserRestriction::getUserId));
    }
}
