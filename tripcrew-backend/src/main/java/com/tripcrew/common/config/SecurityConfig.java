package com.tripcrew.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * REST + JWT 기반 보안 설정.
 *
 * <p>세션을 쓰지 않는 STATELESS 구성으로, JSP 폼 로그인/CSRF 대신 토큰 인증을 전제로 한다.
 * JWT 인증 필터(F01)는 추후 추가하고, 현재는 골격 단계라 전 요청을 permitAll 로 열어 둔다.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // REST API: 브라우저 세션/CSRF 토큰을 쓰지 않음
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // TODO(F01): JWT 인증 필터 추가 후 인가 규칙 세분화
                //   - permitAll: POST /api/auth/**(회원가입/로그인), GET 공개 조회
                //   - authenticated: 그 외
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }

    /** 비밀번호 해시: BCrypt (users.password 는 해시 저장). */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
