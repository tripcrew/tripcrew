package com.tripcrew.auth.oauth;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 소셜 인증 실패 → 프론트 콜백으로 에러코드를 실어 리다이렉트(프론트가 안내 메시지 표시).
 */
@Component
public class OAuth2FailureHandler extends SimpleUrlAuthenticationFailureHandler {

    private final String redirectBase;

    public OAuth2FailureHandler(@Value("${app.oauth2.success-redirect-base}") String redirectBase) {
        this.redirectBase = redirectBase;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException exception) throws IOException, ServletException {
        String error = exception.getMessage();
        String target = UriComponentsBuilder.fromUriString(redirectBase)
                .path("/oauth/callback")
                .queryParam("error", java.net.URLEncoder.encode(
                        error == null ? "social_login_failed" : error, StandardCharsets.UTF_8))
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, target);
    }
}
