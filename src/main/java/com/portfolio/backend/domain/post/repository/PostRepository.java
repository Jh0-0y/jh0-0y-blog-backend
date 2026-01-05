package com.portfolio.backend.domain.post.repository;

import com.portfolio.backend.domain.post.entity.Post;
import com.portfolio.backend.domain.post.entity.PostCategory;
import com.portfolio.backend.domain.post.entity.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    /**
     * 공개된 게시글 목록 조회 (페이징)
     * - 최신순 정렬은 Pageable에서 처리
     *
     * @param status 게시글 상태 (PUBLIC)
     * @param pageable 페이징 정보
     * @return 공개 게시글 페이지
     */
    Page<Post> findByStatus(PostStatus status, Pageable pageable);

    /**
     * 카테고리별 공개 게시글 목록 조회
     *
     * @param category 카테고리
     * @param status 게시글 상태
     * @param pageable 페이징 정보
     * @return 해당 카테고리의 공개 게시글 페이지
     */
    Page<Post> findByCategoryAndStatus(PostCategory category, PostStatus status, Pageable pageable);

    /**
     * 태그명으로 공개 게시글 목록 조회
     * - Post와 Tag의 다대다 관계를 통해 조회
     *
     * @param tagName 태그명
     * @param status 게시글 상태
     * @param pageable 페이징 정보
     * @return 해당 태그가 포함된 공개 게시글 페이지
     */
    @Query("SELECT p FROM Post p JOIN p.tags t WHERE t.name = :tagName AND p.status = :status")
    Page<Post> findByTagNameAndStatus(@Param("tagName") String tagName,
                                      @Param("status") PostStatus status,
                                      Pageable pageable);

    /**
     * 게시글 상세 조회 (태그 정보 함께 로딩)
     * - N+1 문제 방지를 위한 fetch join
     *
     * @param id 게시글 ID
     * @return 태그가 포함된 게시글
     */
    @Query("SELECT p FROM Post p LEFT JOIN FETCH p.tags WHERE p.id = :id")
    Optional<Post> findByIdWithTags(@Param("id") Long id);

    /**
     * 이전 게시글 조회 (현재 게시글보다 ID가 작은 것 중 가장 큰 것)
     * - 공개 게시글만 대상
     *
     * @param id 현재 게시글 ID
     * @param status 게시글 상태
     * @return 이전 게시글
     */
    @Query("SELECT p FROM Post p WHERE p.id < :id AND p.status = :status ORDER BY p.id DESC LIMIT 1")
    Optional<Post> findPreviousPost(@Param("id") Long id, @Param("status") PostStatus status);

    /**
     * 다음 게시글 조회 (현재 게시글보다 ID가 큰 것 중 가장 작은 것)
     * - 공개 게시글만 대상
     *
     * @param id 현재 게시글 ID
     * @param status 게시글 상태
     * @return 다음 게시글
     */
    @Query("SELECT p FROM Post p WHERE p.id > :id AND p.status = :status ORDER BY p.id ASC LIMIT 1")
    Optional<Post> findNextPost(@Param("id") Long id, @Param("status") PostStatus status);

    /**
     * 사용자별 게시글 목록 조회 (관리자용)
     * - 공개/비공개 모두 포함
     *
     * @param userId 사용자 ID
     * @param pageable 페이징 정보
     * @return 해당 사용자의 모든 게시글 페이지
     */
    @Query("SELECT p FROM Post p WHERE p.user.id = :userId")
    Page<Post> findByUserId(@Param("userId") Long userId, Pageable pageable);

    /**
     * 제목 또는 내용으로 검색 (공개 게시글만)
     *
     * @param keyword 검색 키워드
     * @param status 게시글 상태
     * @param pageable 페이징 정보
     * @return 검색 결과 페이지
     */
    @Query("SELECT p FROM Post p WHERE (p.title LIKE %:keyword% OR p.content LIKE %:keyword%) AND p.status = :status")
    Page<Post> searchByKeyword(@Param("keyword") String keyword,
                               @Param("status") PostStatus status,
                               Pageable pageable);
}