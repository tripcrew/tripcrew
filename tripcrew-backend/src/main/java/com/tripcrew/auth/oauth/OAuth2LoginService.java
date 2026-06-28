package com.tripcrew.auth.oauth;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.tripcrew.user.model.Role;
import com.tripcrew.user.model.Status;
import com.tripcrew.user.model.dto.User;
import com.tripcrew.user.model.mapper.UserMapper;

import lombok.RequiredArgsConstructor;

/**
 * 소셜 프로필 → users 매핑(조회/연동/신규가입).
 *
 * <p>정책:
 * <ol>
 *   <li>(provider, provider_id) 로 이미 연동된 계정이 있으면 그 계정으로 로그인.</li>
 *   <li>없으면 이메일이 반드시 있어야 한다(없으면 거부 — 식별·연동 불가).</li>
 *   <li>같은 이메일의 기존 계정이 있으면 <b>검증된 이메일 + ACTIVE</b> 일 때만 자동 연동.
 *       (정지/탈퇴 계정은 소셜로 되살리지 않는다.)</li>
 *   <li>그 외에는 비밀번호 없는 소셜 전용 계정을 새로 만든다.</li>
 * </ol>
 *
 * <p>⚠️ 단순화: users 는 (provider, provider_id) 한 쌍만 가지므로 한 계정에 소셜 1개만 연동된다.
 * 같은 이메일로 두 소셜을 쓰면 마지막 로그인한 제공자로 연동이 덮인다(로그인 자체는 항상 같은 계정).
 * 다중 소셜 연동이 필요하면 별도 user_social_accounts 테이블로 확장(백로그).
 */
@Service
@RequiredArgsConstructor
public class OAuth2LoginService {

    private final UserMapper userMapper;

    @Transactional
    public User resolve(OAuthAttributes attr) {
        // 1) 이미 연동된 소셜 계정
        Optional<User> linked = userMapper.findByProvider(attr.provider().name(), attr.providerId());
        if (linked.isPresent()) {
            return linked.get();
        }

        // 2) 연동/생성에는 이메일이 필수
        if (attr.email() == null || attr.email().isBlank()) {
            throw OAuthAttributes.oauthError("email_required",
                    "이메일 제공에 동의해야 가입할 수 있습니다.");
        }

        // 3) 같은 이메일의 기존 계정 → 검증된 이메일 + ACTIVE 일 때만 자동 연동
        Optional<User> byEmail = userMapper.findByEmail(attr.email());
        if (byEmail.isPresent()) {
            User existing = byEmail.get();
            if (existing.getStatus() != Status.ACTIVE) {
                throw OAuthAttributes.oauthError("account_unavailable",
                        "이용할 수 없는 계정입니다.");
            }
            if (!attr.emailVerified()) {
                throw OAuthAttributes.oauthError("email_not_verified",
                        "이메일이 검증되지 않아 기존 계정과 자동 연동할 수 없습니다.");
            }
            userMapper.linkProvider(existing.getId(), attr.provider().name(), attr.providerId());
            existing.setProvider(attr.provider());
            existing.setProviderId(attr.providerId());
            return existing;
        }

        // 4) 신규 소셜 전용 계정(비밀번호 없음)
        User created = User.builder()
                .email(attr.email())
                .password(null)
                .nickname(safeNickname(attr.nickname(), attr.email()))
                .role(Role.USER)
                .provider(attr.provider())
                .providerId(attr.providerId())
                .build();
        userMapper.insert(created);
        return created;
    }

    /** 닉네임은 NOT NULL(최대 50자). 제공자가 안 주면 이메일 로컬파트로 대체. */
    private String safeNickname(String nickname, String email) {
        String base = (nickname != null && !nickname.isBlank())
                ? nickname.trim()
                : email.substring(0, email.indexOf('@'));
        return base.length() > 50 ? base.substring(0, 50) : base;
    }
}
