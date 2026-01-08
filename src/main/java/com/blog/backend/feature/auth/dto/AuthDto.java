package com.blog.backend.feature.auth.dto;

import com.blog.backend.feature.auth.entity.User;
import com.blog.backend.feature.auth.entity.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class AuthDto {

    // ========== Request ========== //

    /**
     * 회원가입 요청
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SignUpRequest {

        @NotBlank(message = "이메일을 입력해주세요")
        @Email(message = "올바른 이메일 형식이 아닙니다")
        private String email;

        @NotBlank(message = "비밀번호를 입력해주세요")
        @Size(min = 8, max = 20, message = "비밀번호는 8~20자로 입력해주세요")
        private String password;

        @NotBlank(message = "닉네임을 입력해주세요")
        @Size(min = 2, max = 20, message = "닉네임은 2~20자로 입력해주세요")
        private String nickname;
    }

    /**
     * 로그인 요청
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LoginRequest {

        @NotBlank(message = "이메일을 입력해주세요")
        @Email(message = "올바른 이메일 형식이 아닙니다")
        private String email;

        @NotBlank(message = "비밀번호를 입력해주세요")
        private String password;
    }

    // ========== Response ========== //

    /**
     * 로그인/회원가입 응답
     * - 토큰은 HttpOnly 쿠키로 전달
     * - 응답 body에는 사용자 정보만 포함
     */
    @Getter
    @Builder
    public static class LoginResponse {

        private UserInfoResponse user;

        public static LoginResponse from(User user) {
            return LoginResponse.builder()
                    .user(UserInfoResponse.from(user))
                    .build();
        }
    }

    /**
     * 사용자 정보 응답
     */
    @Getter
    @Builder
    public static class UserInfoResponse {

        private Long id;
        private String email;
        private String nickname;
        private UserRole role;

        public static UserInfoResponse from(User user) {
            return UserInfoResponse.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .nickname(user.getNickname())
                    .role(user.getRole())
                    .build();
        }
    }
}