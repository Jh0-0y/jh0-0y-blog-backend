package com.blog.backend.persentation.auth;

import com.blog.backend.application.auth.AuthService;
import com.blog.backend.global.common.ApiResponse;
import com.blog.backend.infra.security.CustomUserDetails;
import com.blog.backend.persentation.auth.dto.AuthDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class MeController {

    private final AuthService authService;

    /**
     * 내 정보 조회
     * GET /api/me
     *
     * @AuthenticationPrincipal: SecurityContext에서 인증된 사용자 정보 주입
     */
    @GetMapping()
    public ResponseEntity<ApiResponse<AuthDto.UserInfoResponse>> getMe(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        AuthDto.UserInfoResponse response = authService.getMe(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
