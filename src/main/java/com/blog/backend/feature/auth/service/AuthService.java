package com.blog.backend.feature.auth.service;

import com.blog.backend.feature.auth.entity.User;
import com.blog.backend.feature.auth.dto.AuthDto;
import com.blog.backend.global.error.CustomException;

public interface AuthService {

    /**
     * 회원가입
     * @param request 회원가입 요청 DTO
     * @return 생성된 사용자 엔티티 (컨트롤러에서 쿠키 설정)
     * @throws CustomException 이메일이 이미 존재하는 경우 (CONFLICT)
     * @throws CustomException 닉네임이 이미 존재하는 경우 (CONFLICT)
     */
    User signUp(AuthDto.SignUpRequest request);

    /**
     * 로그인
     * @param request 로그인 요청 DTO
     * @return 인증된 사용자 엔티티 (컨트롤러에서 쿠키 설정)
     * @throws CustomException 사용자를 찾을 수 없는 경우 (UNAUTHORIZED)
     * @throws CustomException 비밀번호가 일치하지 않는 경우 (UNAUTHORIZED)
     */
    User login(AuthDto.LoginRequest request);

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
    AuthDto.UserInfoResponse getMe(Long userId);
}