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
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserValidatorService userValidatorService;
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

        // 2. 닉네임 수정 (요청이 있고, 값이 있는 경우)
        if (request != null && request.getNickname() != null && !request.getNickname().isBlank()) {
            // 기존 닉네임과 다른 경우에만 중복 검사 및 업데이트
            if (!user.getNickname().equals(request.getNickname())) {
                userValidatorService.validateDuplicateNickname(request.getNickname());
                user.updateNickname(request.getNickname());
                log.info("닉네임 수정 완료: userId={}, newNickname={}", userId, request.getNickname());
            }
        }

        // 4. 변경사항 저장 및 응답 반환
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

    // ========== Private Methods ========== //

    /**
     * 프로필 이미지 수정 로직
     * - 기존 이미지가 있으면 S3 + FileMetadata 삭제
     * - 새 이미지를 S3에 업로드
     * - User 엔티티에 URL 업데이트
     * - UserFile 중간테이블에 fileId 업데이트 (또는 새로 생성)
     */
    private void updateProfileImage(User user, MultipartFile profileImage) throws IOException {
//        log.info("프로필 이미지 수정 시작: userId={}", user.getId());
//
//        // 1. 기존 프로필 이미지 삭제
//        deleteExistingProfileImage(user.getId());
//
//        // 2. 새 프로필 이미지 S3 업로드
//        FileMetadata newFileMetadata = s3FileService.uploadPublicImage(profileImage);
//        log.info("새 프로필 이미지 업로드 완료: fileId={}, url={}",
//                newFileMetadata.getId(), newFileMetadata.getUrl());
//
//        // 3. User 엔티티에 새 이미지 URL 저장
//        user.updateProfileImageUrl(newFileMetadata.getUrl());
//
//        // 4. UserFile 중간테이블에 매핑 정보 저장 (기존 레코드가 있으면 fileId만 업데이트)
//        UserFile userFile = userFileRepository.findByUserIdAndFileType(user.getId(), UserFileType.PROFILE)
//                .orElse(null);
//
//        if (userFile != null) {
//            // 기존 레코드가 있으면 fileId만 업데이트 (UserFile은 불변 엔티티이므로 삭제 후 재생성)
//            userFileRepository.delete(userFile);
//            log.info("기존 UserFile 레코드 삭제: userFileId={}", userFile.getId());
//        }
//
//        // 새 UserFile 레코드 생성
//        UserFile newUserFile = UserFile.builder()
//                .userId(user.getId())
//                .fileId(newFileMetadata.getId())
//                .fileType(UserFileType.PROFILE)
//                .build();
//
//        userFileRepository.save(newUserFile);
//        log.info("새 UserFile 레코드 생성: userFileId={}, fileId={}",
//                newUserFile.getId(), newFileMetadata.getId());
    }

    /**
     * 기존 프로필 이미지 삭제
     * - S3 파일 삭제
     * - FileMetadata 레코드 삭제
     */
    private void deleteExistingProfileImage(Long userId) {
//        log.info("기존 프로필 이미지 삭제 시작: userId={}", userId);
//
//        // 1. UserFile에서 기존 프로필 이미지 매핑 조회
//        userFileRepository.findByUserIdAndFileType(userId, UserFileType.PROFILE)
//                .ifPresent(userFile -> {
//                    // 2. FileMetadata 조회
//                    fileMetadataRepository.findById(userFile.getFileId())
//                            .ifPresent(fileMetadata -> {
//                                // 3. S3 파일 + FileMetadata 삭제
//                                s3FileService.deleteFile(fileMetadata);
//                                log.info("기존 프로필 이미지 삭제 완료: fileId={}, s3Key={}",
//                                        fileMetadata.getId(), fileMetadata.getS3Key());
//                            });
//                });
    }
}
