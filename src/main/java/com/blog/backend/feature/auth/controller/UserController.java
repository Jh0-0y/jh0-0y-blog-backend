package com.blog.backend.feature.auth.controller;

import com.blog.backend.feature.auth.dto.UserRequest;
import com.blog.backend.feature.auth.service.AuthService;
import com.blog.backend.global.core.response.ApiResponse;
import com.blog.backend.global.core.exception.CustomException;
import com.blog.backend.global.security.auth.CustomUserDetails;
import com.blog.backend.feature.auth.dto.UserResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class UserController {

    private final AuthService authService;

    /**
     * 내 정보 조회
     * GET /api/me
     */
    @GetMapping
    public ResponseEntity<ApiResponse<UserResponse.UserInfo>> getMe(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        UserResponse.UserInfo response = authService.getMe(userDetails.getUserId());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 프로필 수정
     * PATCH /api/me/profile
     * - 닉네임 및/또는 프로필 이미지 수정
     * - MultipartFile과 JSON을 함께 전송하기 위해 @RequestPart 사용
     */
    @PatchMapping("/profile")
    public ResponseEntity<ApiResponse<UserResponse.UserInfo>> updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestPart(required = false) @Valid UserRequest.UpdateProfileRequest request,
            @RequestPart(required = false) MultipartFile profileImage
    ) {
        try {
            // 닉네임과 이미지 둘 다 없으면 에러
            if ((request == null || request.getNickname() == null || request.getNickname().isBlank())
                    && (profileImage == null || profileImage.isEmpty())) {
                throw CustomException.badRequest("수정할 정보를 입력해주세요");
            }

            UserResponse.UserInfo response = authService.updateProfile(
                    userDetails.getUserId(),
                    request,
                    profileImage
            );

            return ResponseEntity.ok(ApiResponse.success(response, "프로필이 수정되었습니다"));
        } catch (java.io.IOException e) {
            throw CustomException.badRequest("파일 업로드 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 비밀번호 변경
     * PATCH /api/me/password
     * - 현재 비밀번호 확인 후 새 비밀번호로 변경
     */
    @PatchMapping("/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UserRequest.ChangePasswordRequest request
    ) {
        authService.changePassword(userDetails.getUserId(), request);
        return ResponseEntity.ok(ApiResponse.success(null, "비밀번호가 변경되었습니다"));
    }
}