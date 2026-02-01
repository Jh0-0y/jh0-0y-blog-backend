package com.blog.backend.feature.user.service;

import com.blog.backend.global.file.collector.FileUsageCollector;

/**
 * UserFile 중간 테이블 관리 서비스
 *
 * 역할:
 * - User와 FileMetadata 간의 매핑 관리
 * - 프로필 이미지 연결/삭제 처리
 * - 사용자가 실제 사용하는 파일 조회
 */
public interface UserFileService extends FileUsageCollector {

    /**
     * 프로필 이미지 매핑 생성
     *
     * @param userId 사용자 ID
     * @param fileId 파일 ID
     */
    void saveProfileMapping(Long userId, Long fileId);

    /**
     * 사용자의 기존 프로필 이미지 매핑 삭제
     *
     * @param userId 사용자 ID
     */
    void deleteExistingProfile(Long userId);

    /**
     * 사용자의 프로필 이미지 파일 ID 조회
     *
     * @param userId 사용자 ID
     * @return 프로필 파일 ID (없으면 null)
     */
    Long getProfileFileId(Long userId);
}