package com.blog.backend.global.file.scheduler;

import com.blog.backend.global.file.collector.FileUsageCollector;
import com.blog.backend.global.file.entity.FileMetadata;
import com.blog.backend.global.file.service.FileMetadataService;
import com.blog.backend.infra.s3.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 파일 정리 스케줄러 (합집합-차집합 전략)
 *
 * 동작 방식:
 * 1. 전체 수집: 모든 FileUsageCollector 구현체로부터 사용 중인 파일 ID 수집
 * 2. 합집합 생성: 수집한 ID들을 하나의 Set으로 통합
 * 3. 차집합 조회: FileMetadata에서 합집합에 포함되지 않은 파일 조회
 * 4. S3 멀티 삭제: 조회된 파일들을 한 번의 요청으로 삭제 (1000개씩 분할)
 * 5. DB 정리: S3 삭제 성공한 파일만 DB에서 삭제
 *
 * 확장성:
 * - 새로운 중간 테이블 추가 시 해당 서비스가 FileUsageCollector만 구현하면 자동 반영
 * - 예: UserFileService, CommentFileService 등
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FileCleanupScheduler {

    private final S3Service s3Service;
    private final FileMetadataService fileMetadataService;
    private final List<FileUsageCollector> fileUsageCollectors;

    /**
     * 매일 새벽 3시에 사용되지 않는 파일을 정리합니다.
     *
     * 트랜잭션 전략:
     * - S3 삭제 성공한 파일만 DB에서 삭제
     * - 일부 실패 시에도 성공한 것들은 정리 완료
     */
    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void cleanupUnusedFiles() {
        log.info("=== 파일 정리 스케줄러 시작 (합집합-차집합 전략) ===");

        try {
            // 1. 모든 도메인에서 사용 중인 파일 ID 수집
            Set<Long> usedFileIds = collectAllUsedFileIds();

            if (usedFileIds.isEmpty()) {
                log.warn("=== 사용 중인 파일 ID가 하나도 없음 - 안전을 위해 삭제 스킵 ===");
                return;
            }

            log.info("사용 중인 파일 ID 수집 완료: count={}", usedFileIds.size());

            // 2. 사용되지 않는 파일 조회 (24시간 경과 + 사용 중이 아닌 파일)
            List<FileMetadata> unusedFiles = fileMetadataService.findUnusedFiles(usedFileIds, 24);

            if (unusedFiles.isEmpty()) {
                log.info("=== 삭제할 파일이 없습니다 ===");
                return;
            }

            log.info("삭제 대상 파일 발견: count={}", unusedFiles.size());

            // 3. S3에서 파일 멀티 삭제 (성공한 것만 반환)
            List<String> s3Keys = unusedFiles.stream()
                    .map(FileMetadata::getPath)
                    .collect(Collectors.toList());

            List<String> deletedS3Keys = s3Service.deleteFiles(s3Keys);

            if (deletedS3Keys.isEmpty()) {
                log.warn("=== S3 파일 삭제가 모두 실패했습니다 ===");
                return;
            }

            // 4. S3 삭제 성공한 파일의 ID 추출
            Set<String> deletedS3KeySet = new HashSet<>(deletedS3Keys);
            List<Long> fileIdsToDelete = unusedFiles.stream()
                    .filter(file -> deletedS3KeySet.contains(file.getPath()))
                    .map(FileMetadata::getId)
                    .collect(Collectors.toList());

            // 5. DB에서 메타데이터 벌크 삭제
            fileMetadataService.deleteFileMetadataByIds(fileIdsToDelete);

            int failedCount = unusedFiles.size() - fileIdsToDelete.size();
            log.info("=== 파일 정리 완료: S3 삭제 성공={}, DB 삭제 성공={}, 실패={}, 전체={} ===",
                    deletedS3Keys.size(), fileIdsToDelete.size(), failedCount, unusedFiles.size());

        } catch (Exception e) {
            log.error("파일 정리 스케줄러 실행 실패: {}", e.getMessage(), e);
        }
    }

    /**
     * 모든 FileUsageCollector로부터 사용 중인 파일 ID를 수집하여 합집합 생성
     *
     * 현재 구현된 컬렉터:
     * - PostFileService: 게시글에서 사용 중인 파일
     * - UserFileService: 사용자 프로필 등에서 사용 중인 파일
     *
     * 향후 추가 예정
     * - CommentFileService: 댓글에서 사용 중인 파일
     *
     * @return 사용 중인 파일 ID 합집합
     */
    private Set<Long> collectAllUsedFileIds() {
        Set<Long> allUsedFileIds = new HashSet<>();

        for (FileUsageCollector collector : fileUsageCollectors) {
            try {
                Set<Long> collectorFileIds = collector.collectUsedFileIds();
                allUsedFileIds.addAll(collectorFileIds);

                log.info("파일 ID 수집: collector={}, count={}",
                        collector.getClass().getSimpleName(), collectorFileIds.size());

            } catch (Exception e) {
                log.error("파일 ID 수집 실패: collector={}, error={}",
                        collector.getClass().getSimpleName(), e.getMessage(), e);
                // 한 컬렉터가 실패해도 다른 컬렉터는 계속 실행
            }
        }

        return allUsedFileIds;
    }
}