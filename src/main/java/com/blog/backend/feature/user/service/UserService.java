package com.blog.backend.feature.user.service;

import com.blog.backend.feature.user.dto.UserRequest;
import com.blog.backend.feature.user.dto.UserResponse;
import com.blog.backend.global.core.exception.CustomException;

public interface UserService {

    /**
     * 현재 로그인한 사용자 정보 조회
     * @param userId 사용자 ID
     * @return 사용자 정보
     * @throws CustomException 사용자를 찾을 수 없는 경우 (NOT_FOUND)
     */
    UserResponse.UserInfo getMe(Long userId);

    /**
     * 프로필 정보 수정 (닉네임 및/또는 프로필 이미지)
     * @param userId 사용자 ID
     * @param request 프로필 수정 요청 DTO (닉네임, fileId, profileUrl)
     * @return 수정된 사용자 정보
     * @throws CustomException 사용자를 찾을 수 없는 경우 (NOT_FOUND)
     * @throws CustomException 닉네임이 이미 존재하는 경우 (CONFLICT)
     * @throws CustomException 파일 업로드 실패 시 (INTERNAL_SERVER_ERROR)
     */
    UserResponse.UserInfo updateProfile(
            Long userId,
            UserRequest.UpdateProfileRequest request
    );

    /**
     * 비밀번호 변경
     * @param userId 사용자 ID
     * @param request 비밀번호 변경 요청 DTO
     * @throws CustomException 사용자를 찾을 수 없는 경우 (NOT_FOUND)
     * @throws CustomException 현재 비밀번호가 일치하지 않는 경우 (BAD_REQUEST)
     * @throws CustomException 새 비밀번호 확인이 일치하지 않는 경우 (BAD_REQUEST)
     */
    void changePassword(Long userId, UserRequest.ChangePasswordRequest request);
}
