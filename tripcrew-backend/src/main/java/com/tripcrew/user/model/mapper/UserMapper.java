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

    Optional<User> findById(Long id);

    /** 성공 시 user.id 에 생성된 PK가 채워진다(useGeneratedKeys). */
    int insert(User user);

    /** 관리자용 전체 사용자 목록 (비밀번호 제외). */
    List<User> findAll();

    /** 관리자용 role 변경. affected rows 0 이면 대상 없음. */
    int updateRole(@Param("id") Long id, @Param("role") Role role);

    /** 관리자용 계정 상태(밴/해제) 변경. affected rows 0 이면 대상 없음. */
    int updateStatus(@Param("id") Long id, @Param("status") Status status);

    int updateNickname(@Param("id") Long id, @Param("nickname") String nickname);

    /** 신고 처리완료 시 누적 신고 횟수 +1. (3회 이상이면 호출측에서 자동 제재) */
    int incrementReportCount(@Param("id") Long id);
}
