package com.tripcrew.auth.service;

import java.time.Duration;
import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tripcrew.auth.exception.BannedUserException;
import com.tripcrew.auth.exception.DuplicateEmailException;
import com.tripcrew.auth.exception.InvalidCredentialsException;
import com.tripcrew.auth.exception.InvalidTokenException;
import com.tripcrew.auth.jwt.JwtProvider;
import com.tripcrew.auth.model.dto.LoginRequest;
import com.tripcrew.auth.model.dto.RefreshToken;
import com.tripcrew.auth.model.dto.SignupRequest;
import com.tripcrew.auth.model.dto.TokenResponse;
import com.tripcrew.auth.model.dto.UserResponse;
import com.tripcrew.auth.model.mapper.RefreshTokenMapper;
import com.tripcrew.user.model.Role;
import com.tripcrew.user.model.Status;
import com.tripcrew.user.model.dto.User;
import com.tripcrew.user.model.mapper.UserMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final RefreshTokenMapper refreshTokenMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    /** 회원가입. 이메일 중복 시 409. */
    @Transactional
    public UserResponse signup(SignupRequest request) {
        if (userMapper.existsByEmail(request.email())) {
            throw new DuplicateEmailException();
        }
        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .nickname(request.nickname())
                .role(Role.USER)
                .build();
        userMapper.insert(user);
        return UserResponse.from(user);
    }

    /** 로그인. 자격증명 불일치 시 401. */
    @Transactional
    public TokenResponse login(LoginRequest request) {
        User user = userMapper.findByEmail(request.email())
                .orElseThrow(InvalidCredentialsException::new);
        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new InvalidCredentialsException();
        }
        if (user.getStatus() == Status.BANNED) {
            throw new BannedUserException();
        }
        return issueTokens(user);
    }

    /** 리프레시 토큰으로 토큰 재발급(회전). 유효하지 않으면 401. */
    @Transactional
    public TokenResponse reissue(String refreshToken) {
        if (!jwtProvider.validate(refreshToken)) {
            throw new InvalidTokenException();
        }
        RefreshToken stored = refreshTokenMapper.findByToken(refreshToken)
                .orElseThrow(InvalidTokenException::new);
        if (stored.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenMapper.deleteByToken(refreshToken);
            throw new InvalidTokenException();
        }
        User user = userMapper.findById(stored.getUserId())
                .orElseThrow(InvalidTokenException::new);
        if (user.getStatus() == Status.BANNED) {
            refreshTokenMapper.deleteByToken(refreshToken);
            throw new BannedUserException();
        }
        return issueTokens(user);
    }

    /** 로그아웃: 해당 사용자의 리프레시 토큰 폐기. */
    @Transactional
    public void logout(Long userId) {
        refreshTokenMapper.deleteByUserId(userId);
    }

    /** access/refresh 발급 + refresh 는 사용자당 1개로 갱신(회전). */
    private TokenResponse issueTokens(User user) {
        String accessToken = jwtProvider.createAccessToken(user.getId(), user.getRole().name());
        String refreshToken = jwtProvider.createRefreshToken(user.getId());

        refreshTokenMapper.deleteByUserId(user.getId());
        refreshTokenMapper.insert(RefreshToken.builder()
                .userId(user.getId())
                .token(refreshToken)
                .expiresAt(LocalDateTime.now().plus(Duration.ofMillis(jwtProvider.getRefreshTokenExpiration())))
                .build());

        return TokenResponse.of(accessToken, refreshToken, UserResponse.from(user));
    }
}
