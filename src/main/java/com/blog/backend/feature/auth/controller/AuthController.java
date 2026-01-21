package com.blog.backend.feature.auth.controller;

import com.blog.backend.feature.auth.dto.AuthRequest;
import com.blog.backend.feature.auth.service.AuthService;
import com.blog.backend.feature.auth.entity.User;
import com.blog.backend.global.core.response.ApiResponse;
import com.blog.backend.global.core.exception.CustomException;
import com.blog.backend.global.security.util.CookieUtil;
import com.blog.backend.global.security.jwt.JwtTokenProvider;
import com.blog.backend.feature.auth.dto.UserResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 인증 관련 API
 * - 토큰은 HttpOnly 쿠키로 전달
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final JwtTokenProvider jwtTokenProvider;
    private final CookieUtil cookieUtil;

    /**
     * 회원가입
     * POST /api/auth/signup
     */
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<UserResponse.UserInfo>> signUp(
            @Valid @RequestBody AuthRequest.SignUpRequest request,
            HttpServletResponse response
    ) {
        User user = authService.signUp(request);

        // 토큰 생성 및 쿠키 설정
        setTokenCookies(response, user);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(UserResponse.UserInfo.from(user), "회원가입이 완료되었습니다"));
    }

    /**
     * 로그인
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserResponse.UserInfo>> login(
            @Valid @RequestBody AuthRequest.LoginRequest request,
            HttpServletResponse response
    ) {
        User user = authService.login(request);

        // 토큰 생성 및 쿠키 설정
        setTokenCookies(response, user);

        return ResponseEntity.ok(ApiResponse.success(UserResponse.UserInfo.from(user), "로그인 성공"));
    }

    /**
     * 토큰 재발급
     * POST /api/auth/refresh
     * - Refresh Token은 쿠키에서 자동으로 추출
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<UserResponse.UserInfo>> refresh(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        // 쿠키에서 Refresh Token 추출
        String refreshToken = cookieUtil.getRefreshToken(request)
                .orElseThrow(() -> CustomException.unauthorized("Refresh Token이 없습니다"));

        // 토큰 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            cookieUtil.deleteTokenCookies(response);
            throw CustomException.unauthorized("유효하지 않은 Refresh Token입니다");
        }

        // 사용자 조회
        Long userId = jwtTokenProvider.getUserId(refreshToken);
        User user = authService.getUserForRefresh(userId);

        // 새 토큰 생성 및 쿠키 설정
        setTokenCookies(response, user);

        return ResponseEntity.ok(ApiResponse.success(UserResponse.UserInfo.from(user), "토큰이 재발급되었습니다"));
    }

    /**
     * 로그아웃
     * POST /api/auth/logout
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletResponse response) {
        cookieUtil.deleteTokenCookies(response);
        return ResponseEntity.ok(ApiResponse.success(null, "로그아웃 되었습니다"));
    }

    /**
     * 토큰 생성 및 쿠키 설정
     */
    private void setTokenCookies(HttpServletResponse response, User user) {
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId(), user.getEmail());

        cookieUtil.addAccessTokenCookie(response, accessToken);
        cookieUtil.addRefreshTokenCookie(response, refreshToken);
    }
}