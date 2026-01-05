package com.blog.backend.application.auth;

import com.blog.backend.persentation.auth.dto.AuthDto;
import com.blog.backend.domain.user.entity.User;
import com.blog.backend.domain.user.entity.UserRole;
import com.blog.backend.domain.user.repository.UserRepository;
import com.blog.backend.global.error.CustomException;
import com.blog.backend.infra.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    // 회원가입
    @Override
    @Transactional
    public AuthDto.TokenResponse signUp(AuthDto.SignUpRequest request) {
        // 이메일 중복 검사
        validateDuplicateEmail(request.getEmail());

        // 닉네임 중복 검사
        validateDuplicateNickname(request.getNickname());

        // 비밀번호 암호화 및 User 생성
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .role(UserRole.USER)
                .build();

        User savedUser = userRepository.save(user);

        // 토큰 발급
        return generateTokenResponse(savedUser);
    }

    // 로그인
    @Override
    public AuthDto.TokenResponse login(AuthDto.LoginRequest request) {
        // 이메일로 사용자 조회
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> CustomException.unauthorized("이메일 또는 비밀번호가 일치하지 않습니다"));

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw CustomException.unauthorized("이메일 또는 비밀번호가 일치하지 않습니다");
        }

        // 토큰 발급
        return generateTokenResponse(user);
    }

    // 토큰 재발급
    @Override
    public AuthDto.TokenResponse refresh(AuthDto.RefreshRequest request) {
        String refreshToken = request.getRefreshToken();

        // Refresh Token 검증
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw CustomException.unauthorized("유효하지 않은 Refresh Token입니다");
        }

        // 토큰에서 사용자 정보 추출
        Long userId = jwtTokenProvider.getUserId(refreshToken);

        // 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> CustomException.unauthorized("사용자를 찾을 수 없습니다"));

        // 새 토큰 발급
        return generateTokenResponse(user);
    }

    // 현재 사용자 정보 조회
    @Override
    public AuthDto.UserInfoResponse getMe(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> CustomException.notFound("사용자를 찾을 수 없습니다"));

        return AuthDto.UserInfoResponse.from(user);
    }

    // ========== Private Methods ========== //

    /**
     * 이메일 중복 검사
     */
    private void validateDuplicateEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw CustomException.conflict("이미 사용 중인 이메일입니다");
        }
    }

    /**
     * 닉네임 중복 검사
     */
    private void validateDuplicateNickname(String nickname) {
        if (userRepository.existsByNickname(nickname)) {
            throw CustomException.conflict("이미 사용 중인 닉네임입니다");
        }
    }

    /**
     * 토큰 응답 생성
     */
    private AuthDto.TokenResponse generateTokenResponse(User user) {
        String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getEmail());
        String refreshToken = jwtTokenProvider.createRefreshToken(user.getId(), user.getEmail());

        return AuthDto.TokenResponse.of(
                accessToken,
                refreshToken,
                jwtTokenProvider.getAccessTokenValidity(),
                user
        );
    }
}