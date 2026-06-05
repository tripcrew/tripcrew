package com.tripcrew.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.tripcrew.auth.jwt.JwtAuthenticationEntryPoint;
import com.tripcrew.auth.jwt.JwtAuthenticationFilter;
import com.tripcrew.auth.jwt.JwtProvider;

import lombok.RequiredArgsConstructor;

/**
 * REST + JWT 기반 보안 설정.
 *
 * <p>세션을 쓰지 않는 STATELESS 구성. 폼 로그인/CSRF 대신 Bearer 토큰 인증.
 * 요청마다 {@link JwtAuthenticationFilter} 가 토큰을 검증해 인증을 채운다.
 */
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtProvider jwtProvider;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // 공개: 헬스체크, 인증(회원가입/로그인/재발급)
                        .requestMatchers("/api/health").permitAll()
                        .requestMatchers("/api/auth/signup", "/api/auth/login", "/api/auth/reissue").permitAll()
                        // 공개 조회(관광지 등 GET)는 기능 구현 시 개별 permitAll 추가
                        .requestMatchers(HttpMethod.GET, "/api/attractions/**").permitAll()
                        // 그 외는 인증 필요
                        .anyRequest().authenticated())
                .exceptionHandling(e -> e.authenticationEntryPoint(authenticationEntryPoint))
                .addFilterBefore(new JwtAuthenticationFilter(jwtProvider),
                        UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /** 비밀번호 해시: BCrypt (users.password 는 해시 저장). */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
