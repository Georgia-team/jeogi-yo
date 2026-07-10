package com.georgia.jeogiyo.global.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.georgia.jeogiyo.global.security.UserDetailsImpl;
import com.georgia.jeogiyo.user.dto.request.UserLoginRequest;
import com.georgia.jeogiyo.user.entity.Role;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;

/**
 * ──────────────────────────────────────────────────────────────────────────────────────────────────
 * 작성자: 진혜림
 * 작성일: 2026-07-10
 * 설명: 로그인 시도, 로그인 성공, 로그인 실패 처리
 * ──────────────────────────────────────────────────────────────────────────────────────────────────
 */
@Slf4j
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
        setFilterProcessesUrl("/api/v1/auth/login");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        log.info("로그인 시도");
        try {
            UserLoginRequest requestDto = new ObjectMapper().readValue(request.getInputStream(), UserLoginRequest.class);

            return getAuthenticationManager().authenticate( // Spring Security의 인증 매니저에게 위임
                    new UsernamePasswordAuthenticationToken(
                            requestDto.getLoginId(),
                            requestDto.getPassword(),
                            null
                    )
            );
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        log.info("로그인 성공 및 JWT 생성");
        String userId = ((UserDetailsImpl) authResult.getPrincipal()).getUsername();
        Role role = ((UserDetailsImpl) authResult.getPrincipal()).getUser().getRole();

        String token = jwtUtil.createToken(userId, role);
        jwtUtil.addJwtToCookie(token, response);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        log.info("로그인 실패");
        response.setStatus(401);
    }
}
