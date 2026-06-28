package com.tripcrew.user.model.mapper;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.tripcrew.user.model.Role;
import com.tripcrew.user.model.Status;
import com.tripcrew.user.model.dto.User;

@Mapper
public interface UserMapper {

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    /** 소셜 로그인: (provider, provider_id) 로 기존 소셜 계정 조회. */
    Optional<User> findByProvider(@Param("provider") String provider, @Param("providerId") String providerId);

    Optional<User> findById(Long id);

    /** 성공 시 user.id 에 생성된 PK가 채워진다(useGeneratedKeys). */
    int insert(User user);

    /** 관리자용 전체 사용자 목록 (비밀번호 제외). */
    List<User> findAll();

    /** 전체 사용자 수 (관리자 대시보드 집계용). */
    long countAll();

    /** 특정 상태(ACTIVE/BANNED) 사용자 수 (관리자 대시보드 집계용). */
    long countByStatus(@Param("status") Status status);

    /** 관리자용 role 변경. affected rows 0 이면 대상 없음. */
    int updateRole(@Param("id") Long id, @Param("role") Role role);

    /** 관리자용 계정 상태(밴/해제) 변경. affected rows 0 이면 대상 없음. */
    int updateStatus(@Param("id") Long id, @Param("status") Status status);

    int updateNickname(@Param("id") Long id, @Param("nickname") String nickname);

    int updatePassword(@Param("id") Long id, @Param("password") String password);

    /** 소셜 로그인: 기존 계정에 (provider, provider_id) 연동. */
    int linkProvider(@Param("id") Long id, @Param("provider") String provider, @Param("providerId") String providerId);

    /** 신고 처리완료 시 누적 신고 횟수 +1. (임계 도달 시 호출측에서 단계 제재) */
    int incrementReportCount(@Param("id") Long id);

    /** 관리자(ADMIN/SUPER_ADMIN) 사용자 id 목록. 영구정지 검토 알림 발송 대상. */
    List<Long> findAdminIds();
}
