package com.blog.backend.persentation.auth;

import com.blog.backend.persentation.auth.dto.AuthDto;
import com.blog.backend.application.auth.AuthService;
import com.blog.backend.global.common.ApiResponse;
import com.blog.backend.infra.security.CustomUserDetails;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 인증 관련 API
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 회원가입
     * POST /api/auth/signup
     */
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<AuthDto.TokenResponse>> signUp(
            @Valid @RequestBody AuthDto.SignUpRequest request
    ) {
        AuthDto.TokenResponse response = authService.signUp(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "회원가입이 완료되었습니다"));
    }

    /**
     * 로그인
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthDto.TokenResponse>> login(
            @Valid @RequestBody AuthDto.LoginRequest request
    ) {
        AuthDto.TokenResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response, "로그인 성공"));
    }

    /**
     * 토큰 재발급
     * POST /api/auth/refresh
     */
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthDto.TokenResponse>> refresh(
            @Valid @RequestBody AuthDto.RefreshRequest request
    ) {
        AuthDto.TokenResponse response = authService.refresh(request);
        return ResponseEntity.ok(ApiResponse.success(response, "토큰이 재발급되었습니다"));
    }
}