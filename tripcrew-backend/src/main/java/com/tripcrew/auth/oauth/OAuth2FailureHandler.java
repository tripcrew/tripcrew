package com.tripcrew.auth.oauth;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * 소셜 인증 실패 → 프론트 콜백으로 에러코드를 실어 리다이렉트(프론트가 안내 메시지 표시).
 * 원인 진단을 위해 제공자 원본 에러(코드+설명)는 서버 로그에 남긴다(사용자에겐 코드만 노출).
 */
@Slf4j
@Component
public class OAuth2FailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final String redirectBase;

    public OAuth2FailureHandler(@Value("${app.oauth2.success-redirect-base}") String redirectBase) {
        this.redirectBase = redirectBase;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException {
        if (exception instanceof OAuth2AuthenticationException oae) {
            log.warn("OAuth2 login failed: code={}, description={}, uri={}",
                    oae.getError().getErrorCode(), oae.getError().getDescription(), oae.getError().getUri());
        } else {
            log.warn("OAuth2 login failed: {}", exception.getMessage());
        }
        String error = exception.getMessage();
        String target = UriComponentsBuilder.fromUriString(redirectBase)
                .path("/oauth/callback")
                .queryParam("error", java.net.URLEncoder.encode(
                        error == null ? "social_login_failed" : error, StandardCharsets.UTF_8))
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, target);
    }
}
