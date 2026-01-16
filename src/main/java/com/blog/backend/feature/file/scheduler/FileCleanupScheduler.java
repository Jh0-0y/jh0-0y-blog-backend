package com.blog.backend.feature.file.scheduler;

import com.blog.backend.feature.file.service.FileMetadataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 고아 파일 정리 스케줄러
 *
 * 매일 새벽 3시에 실행되어 어떤 매핑 테이블에도 속하지 않고
 * 24시간 이상 경과한 파일을 삭제합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FileCleanupScheduler {

    private final FileMetadataService fileMetadataService;

    /**
     * 매일 새벽 3시에 고아 파일을 정리합니다.
     *
     * 24시간 기준:
     * - 사용자가 파일 업로드 후 게시글 작성을 포기할 수 있는 충분한 시간
     * - 짧은 기간(2시간)은 작성 중인 사용자의 파일을 삭제할 위험 존재
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void cleanupOrphanFiles() {
        log.info("=== 고아 파일 정리 스케줄러 시작 ===");

        try {
            int deletedCount = fileMetadataService.deleteOrphanFiles(24);
            log.info("=== 고아 파일 정리 완료: 삭제된 파일={} ===", deletedCount);
        } catch (Exception e) {
            log.error("고아 파일 정리 실패: {}", e.getMessage(), e);
        }
    }
}