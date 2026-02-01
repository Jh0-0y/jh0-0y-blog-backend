package com.blog.backend.feature.user.service;

import com.blog.backend.feature.user.entity.UserFile;
import com.blog.backend.feature.user.entity.UserFileType;
import com.blog.backend.feature.user.repository.UserFileRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * UserFile 중간 테이블 관리 서비스 구현체
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserFileServiceImpl implements UserFileService {

    private final UserFileRepository userFileRepository;

    @Override
    @Transactional
    public void saveProfileMapping(Long userId, Long fileId) {
        UserFile profileMapping = UserFile.builder()
                .userId(userId)
                .fileId(fileId)
                .fileType(UserFileType.PROFILE)
                .build();

        userFileRepository.save(profileMapping);
        log.info("프로필 이미지 매핑 저장 완료: userId={}, fileId={}", userId, fileId);
    }

    @Override
    @Transactional
    public void deleteExistingProfile(Long userId) {
        Optional<UserFile> existingProfile = userFileRepository
                .findByUserIdAndFileType(userId, UserFileType.PROFILE);

        if (existingProfile.isPresent()) {
            userFileRepository.delete(existingProfile.get());
            log.info("기존 프로필 이미지 매핑 삭제 완료: userId={}, fileId={}",
                    userId, existingProfile.get().getFileId());
        } else {
            log.info("삭제할 프로필 이미지 매핑이 없음: userId={}", userId);
        }
    }

    @Override
    public Long getProfileFileId(Long userId) {
        Optional<UserFile> profile = userFileRepository
                .findByUserIdAndFileType(userId, UserFileType.PROFILE);

        if (profile.isPresent()) {
            Long fileId = profile.get().getFileId();
            log.info("프로필 이미지 파일 ID 조회 완료: userId={}, fileId={}", userId, fileId);
            return fileId;
        }

        log.info("프로필 이미지 파일이 없음: userId={}", userId);
        return null;
    }

    @Override
    public Set<Long> collectUsedFileIds() {
        List<Long> usedFileIds = userFileRepository.findAllUsedFileIds();
        Set<Long> uniqueFileIds = Set.copyOf(usedFileIds);
        log.info("UserFile에서 사용 중인 파일 ID 수집 완료: count={}", uniqueFileIds.size());
        return uniqueFileIds;
    }
}