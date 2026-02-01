package com.blog.backend.feature.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class UserRequest {

    /**
     * 프로필 수정 요청
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateProfileRequest {

        @Size(min = 2, max = 20, message = "닉네임은 2~20자로 입력해주세요")
        private String nickname;
        @Size(max = 50, message = "포지션은 50글자 이내로 입력해주세요")
        private String position;
        @Size(max = 500, message = "소개는 500자 이내로 입력해주세요.")
        private String about;
        private Long profileImageId;
        private String profileImagePath;
        private Boolean removeProfileImage;
    }

    /**
     * 비밀번호 변경 요청
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ChangePasswordRequest {

        @NotBlank(message = "현재 비밀번호를 입력해주세요")
        private String currentPassword;

        @NotBlank(message = "새 비밀번호를 입력해주세요")
        @Size(min = 8, max = 20, message = "비밀번호는 8~20자로 입력해주세요")
        private String newPassword;

        @NotBlank(message = "새 비밀번호 확인을 입력해주세요")
        private String newPasswordConfirm;
    }
}