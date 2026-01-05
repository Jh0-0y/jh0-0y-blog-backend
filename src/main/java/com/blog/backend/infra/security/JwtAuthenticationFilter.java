package com.blog.backend.infra.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 인증 필터
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    // Authorization 헤더 이름
    private static final String AUTHORIZATION_HEADER = "Authorization";

    // Bearer 토큰 접두사
    private static final String BEARER_PREFIX = "Bearer ";

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. 요청 헤더에서 토큰 추출
        String token = resolveToken(request);

        // 2. 토큰 유효성 검증 및 인증 처리
        if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
            // 3. 토큰에서 사용자 정보 추출
            String email = jwtTokenProvider.getEmail(token);

            // 4. UserDetailsService로 사용자 조회
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            // 5. Authentication 객체 생성
            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails,           // Principal (인증된 사용자)
                    null,                  // Credentials (비밀번호, 이미 인증됐으므로 null)
                    userDetails.getAuthorities()  // 권한 목록
            );

            // 6. SecurityContext에 인증 정보 저장
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug("인증 성공: {}", email);
        }

        // 7. 다음 필터로 요청 전달
        filterChain.doFilter(request, response);
    }

    /**
     * 요청 헤더에서 JWT 토큰 추출
     *
     * <p>Authorization 헤더에서 "Bearer " 접두사를 제거하고 토큰만 반환</p>
     *
     * @param request HTTP 요청
     * @return JWT 토큰 (없으면 null)
     */
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader(AUTHORIZATION_HEADER);

        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(BEARER_PREFIX.length());
        }

        return null;
    }

    /**
     * 필터를 적용하지 않을 경로 설정
     * - 인증이 필요 없는 경로는 필터 스킵
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();

        // 인증 관련 경로는 필터 스킵
        return path.startsWith("/api/auth/");
    }
}