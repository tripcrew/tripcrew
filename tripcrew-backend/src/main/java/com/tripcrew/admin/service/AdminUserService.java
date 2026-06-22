package com.tripcrew.admin.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tripcrew.admin.exception.UserNotFoundException;
import com.tripcrew.admin.model.dto.AdminUserResponse;
import com.tripcrew.auth.model.mapper.RefreshTokenMapper;
import com.tripcrew.common.exception.BusinessException;
import com.tripcrew.user.model.Role;
import com.tripcrew.user.model.Status;
import com.tripcrew.user.model.dto.User;
import com.tripcrew.user.model.mapper.UserMapper;

import lombok.RequiredArgsConstructor;

/**
 * F09 관리자 - 사용자 관리. 모든 진입은 ROLE_ADMIN 으로 SecurityConfig 에서 막혀 있다.
 */
@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserMapper userMapper;
    private final RefreshTokenMapper refreshTokenMapper;

    /** 전체 사용자 목록(id 오름차순). */
    @Transactional(readOnly = true)
    public List<AdminUserResponse> listUsers() {
        return userMapper.findAll().stream()
                .map(AdminUserResponse::from)
                .toList();
    }

    /**
     * 사용자 권한 변경(SUPER_ADMIN 전용, SecurityConfig 에서 보장).
     * SUPER_ADMIN 은 오직 DB 직접 지정으로만 다루고 API 는 절대 건드리지 않는다 —
     * 부여도 회수도 불가. 즉 엔드포인트로는 USER ↔ ADMIN 토글만 허용한다:
     * <ul>
     *   <li>대상 role 값이 SUPER_ADMIN 이면 거부 — 최고 권한 부여 차단.</li>
     *   <li>대상의 현재 role 이 SUPER_ADMIN 이면 거부 — 최고 권한 회수(상호 강등) 차단.</li>
     *   <li>본인을 대상으로 한 변경은 거부 — 자기 자신을 강등해 최고 권한을 잃는 사고 방지.</li>
     * </ul>
     */
    @Transactional
    public void updateRole(Long requesterId, Long targetId, Role role) {
        if (role == Role.SUPER_ADMIN) {
            throw new BusinessException(HttpStatus.BAD_REQUEST,
                    "SUPER_ADMIN 권한은 API 로 부여할 수 없습니다. (DB 직접 지정만 가능)");
        }
        if (requesterId.equals(targetId)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "본인의 권한은 변경할 수 없습니다.");
        }
        User target = userMapper.findById(targetId).orElseThrow(UserNotFoundException::new);
        if (target.getRole() == Role.SUPER_ADMIN) {
            throw new BusinessException(HttpStatus.BAD_REQUEST,
                    "SUPER_ADMIN 권한은 API 로 변경할 수 없습니다. (DB 직접 지정만 가능)");
        }
        userMapper.updateRole(targetId, role);
    }

    /**
     * 사용자 밴(제재). 진입은 ROLE_ADMIN(SUPER_ADMIN 포함)으로 SecurityConfig 에서 막혀 있다.
     * 권한 위계상 제재 대상에 제한을 둔다:
     * <ul>
     *   <li>본인은 밴할 수 없다 — 자기 잠금 사고 방지.</li>
     *   <li>SUPER_ADMIN 은 누구도 밴할 수 없다 — 최고 책임자 보호.</li>
     *   <li>ADMIN 대상은 SUPER_ADMIN 만 밴할 수 있다 — ADMIN 끼리 상호 제재 차단.</li>
     * </ul>
     * 밴 즉시 해당 사용자의 refresh token 을 폐기한다(재발급 차단). 기발급 access token 은
     * stateless 라 만료(최대 30분) 전까지 유효하다 — login/reissue 양쪽에서 BANNED 를 막는다.
     */
    @Transactional
    public void ban(Long requesterId, Long targetId) {
        if (requesterId.equals(targetId)) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "본인 계정은 제재할 수 없습니다.");
        }
        User target = userMapper.findById(targetId).orElseThrow(UserNotFoundException::new);
        if (target.getRole() == Role.SUPER_ADMIN) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "SUPER_ADMIN 계정은 제재할 수 없습니다.");
        }
        if (target.getRole() == Role.ADMIN) {
            User requester = userMapper.findById(requesterId).orElseThrow(UserNotFoundException::new);
            if (requester.getRole() != Role.SUPER_ADMIN) {
                throw new BusinessException(HttpStatus.BAD_REQUEST,
                        "ADMIN 계정은 SUPER_ADMIN 만 제재할 수 있습니다.");
            }
        }
        userMapper.updateStatus(targetId, Status.BANNED);
        refreshTokenMapper.deleteByUserId(targetId);
    }

    /** 사용자 밴 해제. 대상이 없으면 404. */
    @Transactional
    public void unban(Long targetId) {
        User target = userMapper.findById(targetId).orElseThrow(UserNotFoundException::new);
        userMapper.updateStatus(target.getId(), Status.ACTIVE);
    }
}
