package com.blog.backend.global.security.fliter;

import com.blog.backend.global.security.jwt.JwtTokenProvider;
import com.blog.backend.global.security.util.CookieUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;
    private final CookieUtil cookieUtil;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            String accessToken = cookieUtil.getAccessToken(request).orElse(null);
            String refreshToken = cookieUtil.getRefreshToken(request).orElse(null);

            // Case 1: Access Token이 유효한 경우 → 정상 인증
            if (accessToken != null && jwtTokenProvider.validateToken(accessToken)) {
                setAuthentication(accessToken, request);
                log.debug("Access Token 인증 성공");
            }
            // Case 2: Access Token 없거나 만료, Refresh Token 유효 → 자동 재발급
            else if (refreshToken != null && jwtTokenProvider.validateToken(refreshToken)) {
                String email = jwtTokenProvider.getEmail(refreshToken);
                Long userId = jwtTokenProvider.getUserId(refreshToken);

                // 새 Access Token 발급
                String newAccessToken = jwtTokenProvider.createAccessToken(userId, email);

                // 쿠키에 새 Access Token 설정
                cookieUtil.addAccessTokenCookie(response, newAccessToken);

                // 인증 처리
                setAuthentication(newAccessToken, request);

                log.info("Access Token 자동 갱신 완료 - userId: {}, email: {}", userId, email);
            }
            // Case 3: 둘 다 없거나 만료 → 인증 없이 통과 (이후 401 처리됨)
            else {
                log.debug("유효한 토큰 없음 - 인증 없이 진행");
            }

        } catch (Exception e) {
            log.error("인증 처리 중 오류 발생: {}", e.getMessage());
            // 예외 발생해도 필터 체인은 계속 진행 (인증 실패로 처리됨)
        }

        filterChain.doFilter(request, response);
    }

    /**
     * SecurityContext에 인증 정보 설정
     *
     * @param token JWT 토큰
     * @param request HTTP 요청
     */
    private void setAuthentication(String token, HttpServletRequest request) {
        String email = jwtTokenProvider.getEmail(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    /**
     * 필터를 적용하지 않을 경로 설정
     * - 인증 관련 엔드포인트는 필터 제외
     */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.equals("/api/auth/login") ||
                path.equals("/api/auth/signup") ||
                path.equals("/api/auth/refresh") ||
                path.equals("/api/auth/logout");
    }
}