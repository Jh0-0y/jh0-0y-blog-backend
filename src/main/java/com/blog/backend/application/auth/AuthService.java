package com.blog.backend.application.auth;

import com.blog.backend.persentation.auth.dto.AuthDto;
import com.blog.backend.global.error.CustomException;

public interface AuthService {

    /**
     * 회원가입
     * @param request 회원가입 요청 DTO
     * @return 토큰 정보 (Access Token, Refresh Token, 사용자 정보)
     * @throws CustomException 이메일이 이미 존재하는 경우 (CONFLICT)
     * @throws CustomException 닉네임이 이미 존재하는 경우 (CONFLICT)
     */
    AuthDto.TokenResponse signUp(AuthDto.SignUpRequest request);

    /**
     * 로그인
     * @param request 로그인 요청 DTO
     * @return 토큰 정보
     * @throws CustomException 사용자를 찾을 수 없는 경우 (UNAUTHORIZED)
     * @throws CustomException 비밀번호가 일치하지 않는 경우 (UNAUTHORIZED)
     */
    AuthDto.TokenResponse login(AuthDto.LoginRequest request);

    /**
     * 토큰 재발급
     * @param request Refresh Token
     * @return 새로운 토큰 정보
     * @throws CustomException Refresh Token이 유효하지 않은 경우 (UNAUTHORIZED)
     * @throws CustomException 사용자를 찾을 수 없는 경우 (UNAUTHORIZED)
     */
    AuthDto.TokenResponse refresh(AuthDto.RefreshRequest request);

    /**
     * 현재 로그인한 사용자 정보 조회
     *
     * @param userId 사용자 ID
     * @return 사용자 정보
     * @throws CustomException 사용자를 찾을 수 없는 경우 (NOT_FOUND)
     */
    AuthDto.UserInfoResponse getMe(Long userId);
}