package com.blog.backend.feature.file.repository;

import com.blog.backend.feature.file.entity.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 파일 메타데이터 Repository
 */
public interface FileMetadataRepository extends JpaRepository<FileMetadata, Long> {

    /**
     * 고아 파일 조회 (최적화된 쿼리)
     *
     * 고아 파일 판별 기준:
     * 1. PostFile 테이블에 존재하지 않음 (어떤 게시글에도 연결되지 않음)
     * 2. 생성 시간이 기준 시간(예: 현재 - 24시간) 이전
     *
     * 성능 최적화:
     * - NOT EXISTS 사용으로 인덱스 활용
     * - 서브쿼리는 매칭되는 행만 확인 (전체 스캔 방지)
     *
     * @param thresholdTime 기준 시간 (이 시간 이전에 생성된 파일)
     * @return 삭제 대상 고아 파일 목록
     */
    @Query("SELECT f FROM FileMetadata f " +
            "WHERE f.createdAt < :thresholdTime " +
            "AND NOT EXISTS (" +
            "   SELECT 1 FROM PostFile pf WHERE pf.fileId = f.id" +
            ")")
    List<FileMetadata> findOrphanFiles(@Param("thresholdTime") LocalDateTime thresholdTime);

    /**
     * 여러 파일 ID로 파일 메타데이터 조회
     *
     * @param fileIds 파일 ID 목록
     * @return 파일 메타데이터 목록
     */
    List<FileMetadata> findByIdIn(List<Long> fileIds);

    /**
     * 특정 파일 ID들의 존재 여부 확인
     *
     * @param fileIds 파일 ID 목록
     * @return 실제 존재하는 파일 개수
     */
    @Query("SELECT COUNT(f) FROM FileMetadata f WHERE f.id IN :fileIds")
    long countByIdIn(@Param("fileIds") List<Long> fileIds);
}