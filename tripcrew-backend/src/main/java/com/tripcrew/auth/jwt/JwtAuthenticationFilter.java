package com.tripcrew.auth.jwt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * 요청의 Authorization: Bearer 토큰을 검증해 SecurityContext 에 인증 정보를 채운다.
 * 토큰이 없거나 유효하지 않으면 그냥 통과시키고, 인가는 SecurityConfig 규칙이 처리한다.
 */
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String token = resolveToken(request);
        if (token != null && jwtProvider.validate(token)) {
            Long userId = jwtProvider.getUserId(token);
            String role = jwtProvider.getRole(token);
            var authentication = new UsernamePasswordAuthenticationToken(userId, null, toAuthorities(role));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        }
        filterChain.doFilter(request, response);
    }

    /**
     * 토큰의 role 을 권한 목록으로 변환한다. 역할 위계를 여기서 직접 펼친다
     * (Spring Security 6.2 는 RoleHierarchy 빈을 웹 인가에 자동 적용하지 않으므로):
     * SUPER_ADMIN 은 ADMIN 권한도 함께 가져 {@code /api/admin/**}(ROLE_ADMIN) 을 통과한다.
     */
    private List<GrantedAuthority> toAuthorities(String role) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + role));
        if ("SUPER_ADMIN".equals(role)) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }
        return authorities;
    }

    private String resolveToken(HttpServletRequest request) {
        String header = request.getHeader(AUTH_HEADER);
        if (header != null && header.startsWith(BEARER_PREFIX)) {
            return header.substring(BEARER_PREFIX.length());
        }
        return null;
    }
}
