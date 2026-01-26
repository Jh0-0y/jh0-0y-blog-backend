package com.blog.backend.feature.user.dto;

import com.blog.backend.feature.user.entity.User;
import com.blog.backend.feature.user.entity.UserRole;
import lombok.Builder;
import lombok.Getter;

public class UserResponse {

    /**
     * 사용자 정보 응답
     */
    @Getter
    @Builder
    public static class UserInfo {

        private Long id;
        private String email;
        private String nickname;
        private UserRole role;
        private String profileImageUrl;

        public static UserInfo from(User user) {
            return UserInfo.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .nickname(user.getNickname())
                    .role(user.getRole())
                    .profileImageUrl(user.getProfileImageUrl())
                    .build();
        }
    }
}