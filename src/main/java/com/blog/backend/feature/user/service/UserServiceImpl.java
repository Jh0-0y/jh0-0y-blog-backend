package com.blog.backend.feature.user.service;

import com.blog.backend.feature.user.dto.UserRequest;
import com.blog.backend.feature.user.dto.UserResponse;
import com.blog.backend.feature.user.entity.User;
import com.blog.backend.feature.user.repository.UserRepository;
import com.blog.backend.global.core.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserValidatorService userValidatorService;
    private final UserFileService userFileService;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponse.UserInfo getMe(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> CustomException.notFound("사용자를 찾을 수 없습니다"));

        return UserResponse.UserInfo.from(user);
    }

    @Override
    @Transactional
    public UserResponse.UserInfo updateProfile(
            Long userId,
            UserRequest.UpdateProfileRequest request
    ) {
        log.info("프로필 수정 시작: userId={}", userId);

        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> CustomException.notFound("사용자를 찾을 수 없습니다"));

        // 2. 닉네임 수정
        if (request.getNickname() != null && !request.getNickname().isBlank()) {
            if (!user.getNickname().equals(request.getNickname())) {
                userValidatorService.validateDuplicateNickname(request.getNickname());
                user.updateNickname(request.getNickname());
                log.info("닉네임 수정 완료: userId={}, newNickname={}", userId, request.getNickname());
            }
        }

        // 3. 포지션 수정
        if (request.getPosition() != null) {
            user.updatePosition(request.getPosition());
            log.info("포지션 수정 완료: userId={}, position={}", userId, request.getPosition());
        }

        // 4. 소개 수정
        if (request.getAbout() != null) {
            user.updateAbout(request.getAbout());
            log.info("소개 수정 완료: userId={}", userId);
        }

        // 5. 프로필 이미지 처리
        handleProfileImage(user, request);

        // 6. 변경사항 저장 및 응답 반환
        User savedUser = userRepository.save(user);
        log.info("프로필 수정 완료: userId={}", userId);

        return UserResponse.UserInfo.from(savedUser);
    }

    @Override
    @Transactional
    public void changePassword(Long userId, UserRequest.ChangePasswordRequest request) {
        log.info("비밀번호 변경 시작: userId={}", userId);

        // 1. 사용자 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> CustomException.notFound("사용자를 찾을 수 없습니다"));

        // 2. 현재 비밀번호 확인
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            log.warn("현재 비밀번호 불일치: userId={}", userId);
            throw CustomException.badRequest("현재 비밀번호가 일치하지 않습니다");
        }

        // 3. 새 비밀번호 확인 일치 여부 검증
        if (!request.getNewPassword().equals(request.getNewPasswordConfirm())) {
            log.warn("새 비밀번호 확인 불일치: userId={}", userId);
            throw CustomException.badRequest("새 비밀번호가 일치하지 않습니다");
        }

        // 4. 현재 비밀번호와 새 비밀번호가 같은지 확인
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            log.warn("새 비밀번호가 현재 비밀번호와 동일: userId={}", userId);
            throw CustomException.badRequest("새 비밀번호는 현재 비밀번호와 달라야 합니다");
        }

        // 5. 비밀번호 변경
        user.updatePassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        log.info("비밀번호 변경 완료: userId={}", userId);
    }

    @Override
    public UserResponse.BlogUserInfo getBlogUser(String nickname) {
        User user = userRepository.findByNickname(nickname)
                .orElseThrow(() -> CustomException.notFound("사용자를 찾을 수 없습니다"));

        return UserResponse.BlogUserInfo.from(user);
    }

    // ========== Private Methods ========== //

    /**
     * 프로필 이미지 처리
     *
     * 처리 로직:
     * 1. removeProfileImage == true → 기존 매핑 삭제 + User.profileImagePath = null
     * 2. profileImageId != null → 기존 매핑 삭제 + 새 매핑 생성 + User.profileImagePath 업데이트
     * 3. 둘 다 없음 → 프로필 이미지 변경 없음
     */
    private void handleProfileImage(User user, UserRequest.UpdateProfileRequest request) {
        // 1. 프로필 이미지 제거 요청
        if (Boolean.TRUE.equals(request.getRemoveProfileImage())) {
            userFileService.deleteExistingProfile(user.getId());
            user.removeProfileImage();
            log.info("프로필 이미지 제거 완료: userId={}", user.getId());
            return;
        }

        // 2. 새 프로필 이미지로 교체
        if (request.getProfileImageId() != null) {
            // 기존 매핑 삭제
            userFileService.deleteExistingProfile(user.getId());

            // 새 매핑 생성
            userFileService.saveProfileMapping(user.getId(), request.getProfileImageId());

            // User 엔티티에 path 저장
            user.updateProfileImagePath(request.getProfileImagePath());

            log.info("프로필 이미지 교체 완료: userId={}, fileId={}", user.getId(), request.getProfileImageId());
        }

        // 3. 둘 다 없으면 프로필 이미지 변경 없음
    }
}