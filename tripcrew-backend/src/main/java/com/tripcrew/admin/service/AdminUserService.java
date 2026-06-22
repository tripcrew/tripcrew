package com.tripcrew.admin.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tripcrew.admin.exception.UserNotFoundException;
import com.tripcrew.admin.model.dto.AdminUserResponse;
import com.tripcrew.common.exception.BusinessException;
import com.tripcrew.user.model.Role;
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
}
