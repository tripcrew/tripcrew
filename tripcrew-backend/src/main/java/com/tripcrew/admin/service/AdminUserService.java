package com.tripcrew.admin.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tripcrew.admin.exception.UserNotFoundException;
import com.tripcrew.admin.model.dto.AdminUserResponse;
import com.tripcrew.common.exception.BusinessException;
import com.tripcrew.user.model.Role;
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
     * 사용자 권한 변경. 관리자가 자기 자신을 강등해 잠기는 사고를 막기 위해
     * 요청 관리자 본인을 ADMIN 이 아닌 권한으로 바꾸는 것은 거부한다.
     */
    @Transactional
    public void updateRole(Long requesterId, Long targetId, Role role) {
        if (requesterId.equals(targetId) && role != Role.ADMIN) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "본인의 관리자 권한은 해제할 수 없습니다.");
        }
        int affected = userMapper.updateRole(targetId, role);
        if (affected == 0) {
            throw new UserNotFoundException();
        }
    }
}
