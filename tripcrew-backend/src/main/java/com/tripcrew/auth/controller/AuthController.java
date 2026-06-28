package com.tripcrew.auth.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.tripcrew.auth.model.dto.LoginRequest;
import com.tripcrew.auth.model.dto.NicknameUpdateRequest;
import com.tripcrew.auth.model.dto.OAuthExchangeRequest;
import com.tripcrew.auth.model.dto.PasswordUpdateRequest;
import com.tripcrew.auth.model.dto.PasswordVerificationRequest;
import com.tripcrew.auth.model.dto.ReissueRequest;
import com.tripcrew.auth.model.dto.SignupRequest;
import com.tripcrew.auth.model.dto.TokenResponse;
import com.tripcrew.auth.model.dto.UserResponse;
import com.tripcrew.auth.model.dto.WithdrawRequest;
import com.tripcrew.auth.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse signup(@Valid @RequestBody SignupRequest request) {
        return authService.signup(request);
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/reissue")
    public TokenResponse reissue(@Valid @RequestBody ReissueRequest request) {
        return authService.reissue(request.refreshToken());
    }

    /** 소셜 로그인 일회용 코드 → JWT 교환(프론트 콜백에서 호출). */
    @PostMapping("/oauth/exchange")
    public TokenResponse exchangeOAuth(@Valid @RequestBody OAuthExchangeRequest request) {
        return authService.exchangeOAuthCode(request.code());
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@AuthenticationPrincipal Long userId) {
        authService.logout(userId);
    }

    @PostMapping("/me/verify-password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void verifyPassword(@AuthenticationPrincipal Long userId,
            @Valid @RequestBody PasswordVerificationRequest request) {
        authService.verifyPassword(userId, request.currentPassword());
    }

    @PatchMapping("/me/nickname")
    public UserResponse updateNickname(@AuthenticationPrincipal Long userId,
            @Valid @RequestBody NicknameUpdateRequest request) {
        return authService.updateNickname(userId, request.nickname(), request.currentPassword());
    }

    @PatchMapping("/me/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updatePassword(@AuthenticationPrincipal Long userId,
            @Valid @RequestBody PasswordUpdateRequest request) {
        authService.updatePassword(userId, request.currentPassword(), request.newPassword());
    }

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void withdraw(@AuthenticationPrincipal Long userId, @Valid @RequestBody WithdrawRequest request) {
        authService.withdraw(userId, request.currentPassword());
    }
}
