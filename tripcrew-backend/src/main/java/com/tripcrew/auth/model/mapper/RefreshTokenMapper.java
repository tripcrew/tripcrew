package com.tripcrew.auth.model.mapper;

import java.util.Optional;

import org.apache.ibatis.annotations.Mapper;

import com.tripcrew.auth.model.dto.RefreshToken;

@Mapper
public interface RefreshTokenMapper {

    int insert(RefreshToken refreshToken);

    Optional<RefreshToken> findByToken(String token);

    int deleteByToken(String token);

    int deleteByUserId(Long userId);
}
