package com.blog.backend.feature.auth.service;

import com.blog.backend.feature.auth.dto.AuthRequest;
import com.blog.backend.feature.auth.dto.UserRequest;
import com.blog.backend.feature.auth.entity.User;
import com.blog.backend.feature.auth.dto.UserResponse;
import com.blog.backend.global.core.exception.CustomException;

public interface AuthService {

    /**
     * 회원가입
     * @param request 회원가입 요청 DTO
     * @return 생성된 사용자 엔티티 (컨트롤러에서 쿠키 설정)
     * @throws CustomException 이메일이 이미 존재하는 경우 (CONFLICT)
     * @throws CustomException 닉네임이 이미 존재하는 경우 (CONFLICT)
     */
    User signUp(AuthRequest.SignUpRequest request);

    /**
     * 로그인
     * @param request 로그인 요청 DTO
     * @return 인증된 사용자 엔티티 (컨트롤러에서 쿠키 설정)
     * @throws CustomException 사용자를 찾을 수 없는 경우 (UNAUTHORIZED)
     * @throws CustomException 비밀번호가 일치하지 않는 경우 (UNAUTHORIZED)
     */
    User login(AuthRequest.LoginRequest request);

    /**
     * 토큰 재발급을 위한 사용자 조회
     * @param userId Refresh Token에서 추출한 사용자 ID
     * @return 사용자 엔티티
     * @throws CustomException 사용자를 찾을 수 없는 경우 (UNAUTHORIZED)
     */
    User getUserForRefresh(Long userId);

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
     * @param request 프로필 수정 요청 DTO (닉네임, 선택적)
     * @param profileImage 프로필 이미지 파일 (선택적)
     * @return 수정된 사용자 정보
     * @throws CustomException 사용자를 찾을 수 없는 경우 (NOT_FOUND)
     * @throws CustomException 닉네임이 이미 존재하는 경우 (CONFLICT)
     * @throws CustomException 파일 업로드 실패 시 (INTERNAL_SERVER_ERROR)
     */
    UserResponse.UserInfo updateProfile(
            Long userId,
            UserRequest.UpdateProfileRequest request,
            org.springframework.web.multipart.MultipartFile profileImage
    ) throws java.io.IOException;

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