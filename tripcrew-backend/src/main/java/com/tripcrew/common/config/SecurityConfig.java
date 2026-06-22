package com.tripcrew.common.config;

import java.util.List;

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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.tripcrew.auth.jwt.JwtAccessDeniedHandler;
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
    private final JwtAccessDeniedHandler accessDeniedHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
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
                        // 후기 목록 조회는 공개(작성은 인증 필요 → anyRequest 로 커버)
                        .requestMatchers(HttpMethod.GET, "/api/reviews").permitAll()
                        // 역할 변경은 최고 책임자 전용: SUPER_ADMIN 만 다른 사용자 권한을 바꾼다.
                        // 더 좁은 규칙이므로 아래 /api/admin/** 규칙보다 반드시 위에 둔다.
                        .requestMatchers(HttpMethod.PATCH, "/api/admin/users/*/role").hasRole("SUPER_ADMIN")
                        // 관리자 전용(F09): ROLE_ADMIN 필요. anyRequest 보다 반드시 위에 둔다.
                        // (SUPER_ADMIN 은 JWT 필터에서 ROLE_ADMIN 도 함께 부여받아 통과한다.)
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        // 그 외는 인증 필요
                        .anyRequest().authenticated())
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler))
                .addFilterBefore(new JwtAuthenticationFilter(jwtProvider),
                        UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /** 비밀번호 해시: BCrypt (users.password 는 해시 저장). */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of(
                "http://localhost:5173",
                "http://127.0.0.1:5173"
        ));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        configuration.setExposedHeaders(List.of("Authorization"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}
