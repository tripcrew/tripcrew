package com.tripcrew.user.model.mapper;

import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;

import com.tripcrew.user.model.dto.User;

@Mapper
public interface UserMapper {

    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    Optional<User> findById(Long id);

    /** 성공 시 user.id 에 생성된 PK가 채워진다(useGeneratedKeys). */
    int insert(User user);
}
