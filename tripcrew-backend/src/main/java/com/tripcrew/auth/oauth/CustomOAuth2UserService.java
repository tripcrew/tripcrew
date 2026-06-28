package com.tripcrew.auth.oauth;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.tripcrew.user.model.dto.User;

import lombok.RequiredArgsConstructor;

/**
 * 제공자 userInfo 수신 → 우리 users 매핑까지 수행하고,
 * 성공 핸들러가 쓸 수 있도록 우리 user id 를 principal 속성({@code TRIPCREW_USER_ID})에 실어 반환한다.
 *
 * <p>여기서 던지는 {@link OAuth2AuthenticationException} 은 실패 핸들러가 받아 프론트로 에러 리다이렉트.
 */
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    /** 우리 DB user id 를 principal 속성에 싣는 키. 성공 핸들러가 읽는다. */
    public static final String TRIPCREW_USER_ID = "tripcrewUserId";

    private final OAuth2LoginService oAuth2LoginService;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String nameAttributeKey = userRequest.getClientRegistration()
                .getProviderDetails().getUserInfoEndpoint().getUserNameAttributeName();

        OAuthAttributes attributes = OAuthAttributes.of(registrationId, oAuth2User.getAttributes());
        User user = oAuth2LoginService.resolve(attributes);

        Map<String, Object> principalAttributes = new HashMap<>(oAuth2User.getAttributes());
        principalAttributes.put(TRIPCREW_USER_ID, user.getId());

        return new DefaultOAuth2User(
                java.util.List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())),
                principalAttributes,
                nameAttributeKey);
    }
}
