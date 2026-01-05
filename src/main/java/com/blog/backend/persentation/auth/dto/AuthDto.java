package com.blog.backend.persentation.auth.dto;

import com.blog.backend.domain.user.entity.User;
import com.blog.backend.domain.user.entity.UserRole;
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

    /**
     * 토큰 재발급 요청
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RefreshRequest {

        @NotBlank(message = "Refresh Token을 입력해주세요")
        private String refreshToken;
    }

    // ========== Response ========== //

    /**
     * 로그인/회원가입 응답 (토큰 정보)
     */
    @Getter
    @Builder
    public static class TokenResponse {

        private String accessToken;
        private String refreshToken;
        private Long accessTokenExpiresIn;
        private UserInfoResponse user;

        public static TokenResponse of(String accessToken, String refreshToken,
                                       Long expiresIn, User user) {
            return TokenResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .accessTokenExpiresIn(expiresIn)
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