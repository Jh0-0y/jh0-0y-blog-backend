package com.blog.backend.feature.user.dto;

import com.blog.backend.feature.user.entity.User;
import com.blog.backend.feature.user.entity.UserRole;
import jakarta.validation.constraints.Size;
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
        private String name;
        private String nickname;
        private UserRole role;
        private String position;
        private String about;
        private String profileImagePath;

        public static UserInfo from(User user) {
            return UserInfo.builder()
                    .id(user.getId())
                    .email(user.getEmail())
                    .name(user.getName())
                    .nickname(user.getNickname())
                    .role(user.getRole())
                    .position(user.getPosition())
                    .about(user.getAbout())
                    .profileImagePath(user.getProfileImagePath())
                    .build();
        }
    }

    /**
     * 블로그 사용자 정보 응답
     */
    @Getter
    @Builder
    public static class BlogUserInfo {

        private String nickname;
        private String profileImagePath;
        private String position;
        private String about;

        public static BlogUserInfo from(User user) {
            return BlogUserInfo.builder()
                    .nickname(user.getNickname())
                    .profileImagePath(user.getProfileImagePath())
                    .position(user.getPosition())
                    .about(user.getAbout())
                    .build();
        }
    }
}