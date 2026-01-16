package com.blog.backend.feature.auth.dto;

import com.blog.backend.feature.auth.entity.User;
import com.blog.backend.feature.auth.entity.UserRole;
import lombok.Builder;
import lombok.Getter;

public class AuthResponse {

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
        private String profileImageUrl;

        public static UserInfoResponse from(User user) {
            return UserInfoResponse.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .nickname(user.getNickname())
                    .role(user.getRole())
                    .profileImageUrl(user.getProfileImageUrl())
                    .build();
        }
    }
}