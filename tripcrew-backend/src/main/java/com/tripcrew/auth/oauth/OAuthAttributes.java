package com.tripcrew.auth.oauth;

import java.util.Map;

import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;

import com.tripcrew.user.model.Provider;

/**
 * 제공자별로 제각각인 프로필 응답을 공통 형태로 정규화한다.
 *
 * <p>Kakao/Naver 는 표준 OIDC 가 아니라 userInfo 응답 구조가 서로 다르다.
 * <ul>
 *   <li>Kakao: 최상위 {@code id}, 이메일/검증여부는 {@code kakao_account} 안에 중첩, 닉네임은 {@code profile} 안.</li>
 *   <li>Naver: 모든 값이 {@code response} 객체 안에 한 번 감싸져 있다(이메일은 동의 시 검증된 값).</li>
 * </ul>
 */
public record OAuthAttributes(
        Provider provider,
        String providerId,
        String email,
        String nickname,
        boolean emailVerified) {

    public static OAuthAttributes of(String registrationId, Map<String, Object> attributes) {
        return switch (registrationId) {
            case "kakao" -> ofKakao(attributes);
            case "naver" -> ofNaver(attributes);
            default -> throw oauthError("unsupported_provider", "지원하지 않는 소셜 제공자입니다: " + registrationId);
        };
    }

    @SuppressWarnings("unchecked")
    private static OAuthAttributes ofKakao(Map<String, Object> attributes) {
        Object id = attributes.get("id");
        Map<String, Object> account = (Map<String, Object>) attributes.get("kakao_account");
        String email = null;
        boolean verified = false;
        String nickname = null;
        if (account != null) {
            email = (String) account.get("email");
            verified = Boolean.TRUE.equals(account.get("is_email_verified"));
            Map<String, Object> profile = (Map<String, Object>) account.get("profile");
            if (profile != null) {
                nickname = (String) profile.get("nickname");
            }
        }
        return new OAuthAttributes(Provider.KAKAO, String.valueOf(id), email, nickname, verified);
    }

    @SuppressWarnings("unchecked")
    private static OAuthAttributes ofNaver(Map<String, Object> attributes) {
        Map<String, Object> response = (Map<String, Object>) attributes.get("response");
        if (response == null) {
            throw oauthError("invalid_profile", "네이버 프로필 응답이 비어 있습니다.");
        }
        String id = String.valueOf(response.get("id"));
        String email = (String) response.get("email");
        String nickname = (String) response.getOrDefault("nickname", response.get("name"));
        // 네이버는 동의받은 이메일을 검증된 값으로 제공한다.
        boolean verified = email != null && !email.isBlank();
        return new OAuthAttributes(Provider.NAVER, id, email, nickname, verified);
    }

    static OAuth2AuthenticationException oauthError(String code, String message) {
        return new OAuth2AuthenticationException(new OAuth2Error(code, message, null), message);
    }
}
