package com.blog.backend.feature.post.service;

import com.blog.backend.global.file.collector.FileUsageCollector;

import java.util.List;
import java.util.Set;

/**
 * PostFile 중간 테이블 관리 서비스
 *
 * 역할:
 * - Post와 FileMetadata 간의 매핑 관리
 * - 썸네일 및 본문 파일 연결/삭제 처리
 * - 게시글이 실제 사용하는 파일 조회
 */
public interface PostFileService extends FileUsageCollector {

    /**
     * 썸네일 파일 매핑 생성
     *
     * @param postId 게시글 ID
     * @param fileId 파일 ID
     */
    void saveThumbnailMapping(Long postId, Long fileId);

    /**
     * 본문 파일 매핑 생성 (여러 개)
     *
     * @param postId 게시글 ID
     * @param fileIds 파일 ID 목록
     */
    void saveContentFileMappings(Long postId, List<Long> fileIds);

    /**
     * 게시글의 기존 썸네일 매핑 삭제
     *
     * @param postId 게시글 ID
     */
    void deleteExistingThumbnail(Long postId);

    /**
     * 게시글의 특정 본문 파일 매핑들 삭제
     *
     * @param postId 게시글 ID
     * @param fileIds 삭제할 파일 ID 목록
     */
    void deleteContentFileMappings(Long postId, List<Long> fileIds);

    /**
     * 게시글의 모든 파일 매핑 삭제 (게시글 삭제 시)
     *
     * @param postId 게시글 ID
     * @return 삭제된 매핑 개수
     */
    int deleteAllMappingsByPostId(Long postId);

    /**
     * 게시글이 현재 사용 중인 본문 파일 ID 목록 조회
     *
     * @param postId 게시글 ID
     * @return 파일 ID 목록
     */
    Set<Long> getContentFileIds(Long postId);

    /**
     * 게시글이 현재 사용 중인 썸네일 파일 ID 조회
     *
     * @param postId 게시글 ID
     * @return 썸네일 파일 ID (없으면 null)
     */
    Long getThumbnailFileId(Long postId);
}