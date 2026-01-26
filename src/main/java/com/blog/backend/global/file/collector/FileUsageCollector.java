package com.blog.backend.global.file.collector;

import java.util.Set;

/**
 * 파일 사용 현황 수집 인터페이스
 *
 * 각 도메인 서비스(Post, User, Comment 등)가 이 인터페이스를 구현하여
 * 현재 사용 중인 파일 ID 목록을 제공합니다.
 *
 * 사용 목적:
 * - 고아 파일 정리 스케줄러에서 "사용 중인 파일" 합집합 생성
 * - 각 도메인이 자신의 파일 사용 현황을 독립적으로 관리
 *
 * 구현 예시:
 * - PostFileService: PostFile 테이블에서 모든 fileId 조회
 * - UserFileService: UserFile 테이블에서 모든 fileId 조회 (향후)
 * - CommentFileService: CommentFile 테이블에서 모든 fileId 조회 (향후)
 */
public interface FileUsageCollector {

    /**
     * 현재 도메인에서 사용 중인 모든 파일 ID를 조회합니다.
     *
     * 반환 규칙:
     * - 중복 제거된 파일 ID Set 반환
     * - 사용 중인 파일이 없으면 빈 Set 반환 (null 아님)
     *
     * 성능 고려:
     * - DISTINCT 쿼리로 중복 제거
     * - 인덱스를 활용한 빠른 조회
     *
     * @return 사용 중인 파일 ID 집합 (중복 없음)
     */
    Set<Long> collectUsedFileIds();
}