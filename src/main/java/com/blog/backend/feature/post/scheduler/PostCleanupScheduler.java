package com.blog.backend.feature.post.scheduler;

import com.blog.backend.feature.post.entity.Post;
import com.blog.backend.feature.post.repository.PostRepository;
import com.blog.backend.feature.post.service.PostFileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 게시글 정리 스케줄러
 *
 * 매일 자정에 실행되어 7일 이상 삭제 상태인 게시글을 영구 삭제합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PostCleanupScheduler {

    private final PostRepository postRepository;
    private final PostFileService postFileService;

    /**
     * 삭제된 게시글 정리 작업
     *
     * 실행 주기: 매일 자정 (00:00:00)
     *
     * 처리 로직:
     * 1. 7일 이상 삭제 상태(DELETED)인 게시글 조회
     * 2. 각 게시글의 파일 매핑 삭제 (중간테이블)
     * 3. 게시글 스택 관계 초기화
     * 4. 게시글 영구 삭제
     */
    @Scheduled(cron = "0 0 0 * * *") // 매일 자정
    @Transactional
    public void cleanupDeletedPosts() {
        log.info("======================================");
        log.info("게시글 정리 스케줄러 시작");
        log.info("======================================");

        // 7일 이전 시각 계산
        LocalDateTime deletedBefore = LocalDateTime.now().minusDays(7);
        log.info("삭제 기준 시각: {} 이전", deletedBefore);

        // 영구 삭제 대상 게시글 조회
        List<Post> postsToDelete = postRepository.findPostsToHardDelete(deletedBefore);

        if (postsToDelete.isEmpty()) {
            log.info("영구 삭제 대상 게시글 없음");
            log.info("======================================");
            return;
        }

        log.info("영구 삭제 대상 게시글 수: {}", postsToDelete.size());

        int successCount = 0;
        int failCount = 0;

        // 각 게시글 영구 삭제 처리
        for (Post post : postsToDelete) {
            try {
                log.info("게시글 영구 삭제 시작: postId={}, slug={}, deletedAt={}",
                        post.getId(), post.getSlug(), post.getDeletedAt());

                // 1. 모든 파일 매핑 삭제 (중간테이블만)
                log.info("게시글 영구 삭제 - 파일 매핑 삭제 시작: postId={}", post.getId());
                postFileService.deleteAllMappingsByPostId(post.getId());
                log.info("게시글 영구 삭제 - 파일 매핑 삭제 완료: postId={}", post.getId());

                // 2. 스택 관계 초기화
                log.info("게시글 영구 삭제 - 스택 관계 초기화: postId={}", post.getId());
                post.clearStack();

                // 3. 게시글 영구 삭제
                postRepository.delete(post);
                log.info("게시글 영구 삭제 완료: postId={}, slug={}", post.getId(), post.getSlug());

                successCount++;

            } catch (Exception e) {
                log.error("게시글 영구 삭제 실패: postId={}, slug={}, error={}",
                        post.getId(), post.getSlug(), e.getMessage(), e);
                failCount++;
            }
        }

        log.info("======================================");
        log.info("게시글 정리 스케줄러 완료");
        log.info("성공: {}건, 실패: {}건", successCount, failCount);
        log.info("======================================");
    }

    /**
     * 스케줄러 초기화 확인 로그 (애플리케이션 시작 시 1회 실행)
     */
    @Scheduled(initialDelay = 5000, fixedDelay = Long.MAX_VALUE)
    public void logSchedulerStatus() {
        log.info("PostCleanupScheduler 활성화 완료 - 매일 자정(00:00:00) 실행 예정");
    }
}