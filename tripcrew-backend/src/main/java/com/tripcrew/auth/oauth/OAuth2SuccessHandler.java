package com.tripcrew.auth.oauth;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 소셜 인증 성공 → 일회용 코드 발급 후 프론트 콜백으로 리다이렉트.
 * (JWT 는 URL 에 싣지 않고, 프론트가 코드로 교환한다.)
 */
@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final OAuthCodeStore codeStore;
    private final String redirectBase;

    public OAuth2SuccessHandler(OAuthCodeStore codeStore,
            @Value("${app.oauth2.success-redirect-base}") String redirectBase) {
        this.codeStore = codeStore;
        this.redirectBase = redirectBase;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        OAuth2User principal = (OAuth2User) authentication.getPrincipal();
        Long userId = (Long) principal.getAttributes().get(CustomOAuth2UserService.TRIPCREW_USER_ID);
        String code = codeStore.issue(userId);

        String target = UriComponentsBuilder.fromUriString(redirectBase)
                .path("/oauth/callback")
                .queryParam("code", code)
                .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, target);
    }
}
