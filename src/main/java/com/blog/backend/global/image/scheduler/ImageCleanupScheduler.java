package com.blog.backend.global.image.scheduler;

import com.blog.backend.global.image.service.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ImageCleanupScheduler {

    private final ImageService imageService;

    /**
     * 매일 새벽 3시에 고아 이미지 정리
     * - 임시 상태로 24시간 이상 지난 이미지 삭제
     */
    @Scheduled(cron = "0 0 3 * * *")
    public void cleanupOrphanImages() {
        log.info("고아 이미지 정리 스케줄러 시작");

        try {
            int deletedCount = imageService.cleanupOrphanImages();
            log.info("고아 이미지 정리 완료: {}개 삭제", deletedCount);
        } catch (Exception e) {
            log.error("고아 이미지 정리 중 오류 발생", e);
        }
    }
}