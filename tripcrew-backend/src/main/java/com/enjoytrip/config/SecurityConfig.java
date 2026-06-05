package com.enjoytrip.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt 알고리즘을 사용하는 PasswordEncoder를 빈으로 등록합니다.
        // 회원가입 시 이 인코더를 사용하여 비밀번호를 암호화하고, 로그인 시에는 암호화된 비밀번호와 일치하는지 확인합니다.
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF(Cross-Site Request Forgery) 보호를 비활성화합니다.
                // REST API 서버는 세션 기반이 아닌 토큰 기반 인증을 사용하는 경우가 많아 비활성화하는 것이 일반적입니다.
                .csrf(csrf -> csrf.disable())
                // 모든 HTTP 요청에 대해 접근을 허용하도록 설정합니다.
                // 우선 모든 기능을 테스트할 수 있도록 열어두고, 추후 필요에 따라 특정 경로에 인증을 요구하도록 변경할 수 있습니다.
                .authorizeHttpRequests(authorize -> authorize
                        .anyRequest().permitAll()
                );

        return http.build();
    }
}
