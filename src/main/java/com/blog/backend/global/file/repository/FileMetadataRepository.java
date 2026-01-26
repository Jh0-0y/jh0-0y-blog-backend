package com.blog.backend.global.file.repository;

import com.blog.backend.global.file.entity.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

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
     * @deprecated 합집합-차집합 전략으로 대체됨. findUnusedFiles() 사용 권장
     */
    @Deprecated
    @Query("SELECT f FROM FileMetadata f " +
            "WHERE f.createdAt < :thresholdTime " +
            "AND NOT EXISTS (" +
            "   SELECT 1 FROM PostFile pf WHERE pf.fileId = f.id" +
            ")")
    List<FileMetadata> findOrphanFiles(@Param("thresholdTime") LocalDateTime thresholdTime);

    /**
     * 사용되지 않는 파일 조회 (합집합-차집합 전략)
     *
     * 동작 방식:
     * 1. 모든 도메인에서 "사용 중인 파일 ID" 수집 (PostFile, UserFile 등)
     * 2. 수집된 ID들을 합집합으로 생성
     * 3. 전체 파일 중 합집합에 포함되지 않은 파일 조회
     *
     * 장점:
     * - 확장성: 새로운 중간 테이블 추가 시 쿼리 수정 불필요
     * - 명확성: "사용 중"의 정의가 명확함
     * - 안전성: 빈 Set 체크로 실수 방지
     *
     * 주의사항:
     * - usedFileIds가 빈 Set이면 모든 파일이 반환되므로 호출 전 체크 필수
     * - usedFileIds가 1000개 이상이면 분할 처리 권장 (IN 절 제한)
     *
     * @param usedFileIds 사용 중인 파일 ID 집합 (빈 Set이면 안 됨)
     * @param thresholdTime 기준 시간 (이 시간 이전에 생성된 파일만 대상)
     * @return 사용되지 않는 파일 목록
     */
    @Query("SELECT f FROM FileMetadata f " +
            "WHERE f.createdAt < :thresholdTime " +
            "AND f.id NOT IN :usedFileIds")
    List<FileMetadata> findUnusedFiles(
            @Param("usedFileIds") Set<Long> usedFileIds,
            @Param("thresholdTime") LocalDateTime thresholdTime
    );

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