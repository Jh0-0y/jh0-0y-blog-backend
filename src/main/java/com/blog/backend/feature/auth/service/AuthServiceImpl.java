package com.blog.backend.feature.auth.service;

import com.blog.backend.feature.auth.dto.AuthRequest;
import com.blog.backend.feature.auth.dto.AuthResponse;
import com.blog.backend.feature.auth.entity.User;
import com.blog.backend.feature.auth.entity.UserRole;
import com.blog.backend.feature.auth.repository.UserRepository;
import com.blog.backend.global.error.CustomException;
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

    @Override
    @Transactional
    public User signUp(AuthRequest.SignUpRequest request) {
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

        return userRepository.save(user);
    }

    @Override
    public User login(AuthRequest.LoginRequest request) {
        // 이메일로 사용자 조회
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> CustomException.unauthorized("이메일 또는 비밀번호가 일치하지 않습니다"));

        // 비밀번호 검증
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw CustomException.unauthorized("이메일 또는 비밀번호가 일치하지 않습니다");
        }

        return user;
    }

    @Override
    public User getUserForRefresh(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> CustomException.unauthorized("사용자를 찾을 수 없습니다"));
    }

    @Override
    public AuthResponse.UserInfoResponse getMe(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> CustomException.notFound("사용자를 찾을 수 없습니다"));

        return AuthResponse.UserInfoResponse.from(user);
    }

    // ========== Private Methods ========== //

    private void validateDuplicateEmail(String email) {
        if (userRepository.existsByEmail(email)) {
            throw CustomException.conflict("이미 사용 중인 이메일입니다");
        }
    }

    private void validateDuplicateNickname(String nickname) {
        if (userRepository.existsByNickname(nickname)) {
            throw CustomException.conflict("이미 사용 중인 닉네임입니다");
        }
    }
}