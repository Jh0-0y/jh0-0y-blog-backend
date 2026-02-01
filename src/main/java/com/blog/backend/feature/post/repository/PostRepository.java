package com.blog.backend.feature.post.repository;

import com.blog.backend.feature.post.entity.Post;
import com.blog.backend.feature.post.entity.PostType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface PostRepository extends JpaRepository<Post, Long>, JpaSpecificationExecutor<Post> {

    // ========== 존재 여부 확인 ========== //

    /**
     * 특정 제목을 가진 게시글 존재 여부 확인
     */
    boolean existsByTitle(String title);

    /**
     * 특정 slug를 가진 게시글 존재 여부 확인
     */
    boolean existsBySlug(String slug);

    /**
     * 제목으로 게시글 조회 (중복 체크용)
     */
    Optional<Post> findByTitle(String title);

    // ========== Slug 기반 조회 ========== //

    /**
     * slug로 게시글 조회 (PUBLISHED만)
     */
    @Query("SELECT p FROM Post p WHERE p.slug = :slug AND p.status = 'PUBLISHED'")
    Optional<Post> findBySlug(@Param("slug") String slug);

    /**
     * slug로 게시글 상세 조회 (스택, 태그 정보 함께 로딩) - PUBLISHED만
     */
    @Query("SELECT p FROM Post p " +
            "LEFT JOIN FETCH p.stacks " +
            "LEFT JOIN FETCH p.tags " +
            "WHERE p.slug = :slug AND p.status = 'PUBLISHED'")
    Optional<Post> findBySlugWithStacks(@Param("slug") String slug);

    /**
     * slug로 내 게시글 조회 (상태 무관 - 작성자 본인용)
     */
    @Query("SELECT p FROM Post p " +
            "LEFT JOIN FETCH p.stacks " +
            "LEFT JOIN FETCH p.tags " +
            "WHERE p.slug = :slug AND p.user.id = :userId")
    Optional<Post> findBySlugAndUserId(@Param("slug") String slug, @Param("userId") Long userId);

    // ========== Specification 조회 (페이징) ========== //

    /**
     * Specification을 사용한 동적 쿼리 조회
     * - 스택, 태그를 함께 로딩 (N+1 방지)
     */
    @EntityGraph(attributePaths = {"stacks", "tags"})
    @Override
    Page<Post> findAll(Specification<Post> spec, Pageable pageable);

    // ========== 관련 게시글 조회 (PUBLISHED만) ========== //

    /**
     * 1순위 관련 게시글 조회 (Stack 교집합 많음 + PostType 일치 + 최신순)
     */
    @Query("SELECT DISTINCT p FROM Post p " +
            "LEFT JOIN FETCH p.stacks " +
            "LEFT JOIN FETCH p.tags " +
            "JOIN p.stacks s " +
            "WHERE p.id != :currentPostId " +
            "AND p.status = 'PUBLISHED' " +
            "AND p.postType = :postType " +
            "AND s.name IN :stackNames " +
            "GROUP BY p.id " +
            "ORDER BY COUNT(s.id) DESC, p.createdAt DESC")
    List<Post> findRelatedPostsByStackAndType(
            @Param("currentPostId") Long currentPostId,
            @Param("stackNames") List<String> stackNames,
            @Param("postType") PostType postType,
            Pageable pageable
    );

    /**
     * 2순위 관련 게시글 조회 (Stack 교집합 많음 + PostType 다름 + 최신순)
     */
    @Query("SELECT DISTINCT p FROM Post p " +
            "LEFT JOIN FETCH p.stacks " +
            "LEFT JOIN FETCH p.tags " +
            "JOIN p.stacks s " +
            "WHERE p.id != :currentPostId " +
            "AND p.status = 'PUBLISHED' " +
            "AND p.postType != :postType " +
            "AND s.name IN :stackNames " +
            "GROUP BY p.id " +
            "ORDER BY COUNT(s.id) DESC, p.createdAt DESC")
    List<Post> findRelatedPostsByStackOnly(
            @Param("currentPostId") Long currentPostId,
            @Param("stackNames") List<String> stackNames,
            @Param("postType") PostType postType,
            Pageable pageable
    );

    /**
     * 3순위 관련 게시글 조회 (최신 공개 게시글)
     */
    @Query("SELECT p FROM Post p " +
            "LEFT JOIN FETCH p.stacks " +
            "LEFT JOIN FETCH p.tags " +
            "WHERE p.id != :currentPostId " +
            "AND p.status = 'PUBLISHED' " +
            "ORDER BY p.createdAt DESC")
    List<Post> findLatestPublicPosts(
            @Param("currentPostId") Long currentPostId,
            Pageable pageable
    );

    // ========== 삭제된 게시글 조회 (본인용) ========== //

    /**
     * 특정 사용자의 삭제된 게시글 목록 조회 (본인만)
     */
    @Query("SELECT p FROM Post p " +
            "LEFT JOIN FETCH p.stacks " +
            "LEFT JOIN FETCH p.tags " +
            "WHERE p.user.id = :userId " +
            "AND p.status = 'DELETED' " +
            "ORDER BY p.deletedAt DESC")
    Page<Post> findDeletedPostsByUserId(@Param("userId") Long userId, Pageable pageable);

    // ========== 스케줄러용: 영구 삭제 대상 조회 ========== //

    /**
     * N일 이상 경과한 삭제된 게시글 조회 (스케줄러용)
     *
     * @param deletedBefore 이 시각 이전에 삭제된 게시글
     * @return 영구 삭제 대상 게시글 목록
     */
    @Query("SELECT p FROM Post p " +
            "WHERE p.status = 'DELETED' " +
            "AND p.deletedAt IS NOT NULL " +
            "AND p.deletedAt < :deletedBefore")
    List<Post> findPostsToHardDelete(@Param("deletedBefore") LocalDateTime deletedBefore);

    // ========== 자동완성 검색용 ========== //

    /**
     * 제목 부분 일치 검색 (PUBLISHED만)
     */
    @Query("SELECT p FROM Post p " +
            "LEFT JOIN FETCH p.stacks " +
            "LEFT JOIN FETCH p.tags " +
            "LEFT JOIN FETCH p.user " +
            "WHERE p.status = 'PUBLISHED' " +
            "AND p.title LIKE %:keyword% " +
            "ORDER BY p.createdAt DESC")
    List<Post> findByTitleContainingAndPublished(
            @Param("keyword") String keyword,
            Pageable pageable
    );

    /**
     * 설명 부분 일치 검색 (PUBLISHED만)
     */
    @Query("SELECT p FROM Post p " +
            "LEFT JOIN FETCH p.stacks " +
            "LEFT JOIN FETCH p.tags " +
            "LEFT JOIN FETCH p.user " +
            "WHERE p.status = 'PUBLISHED' " +
            "AND p.excerpt LIKE %:keyword% " +
            "ORDER BY p.createdAt DESC")
    List<Post> findByExcerptContainingAndPublished(
            @Param("keyword") String keyword,
            Pageable pageable
    );

    /**
     * 설명 부분 일치 검색 - 특정 ID 제외 (PUBLISHED만)
     */
    @Query("SELECT p FROM Post p " +
            "LEFT JOIN FETCH p.stacks " +
            "LEFT JOIN FETCH p.tags " +
            "LEFT JOIN FETCH p.user " +
            "WHERE p.status = 'PUBLISHED' " +
            "AND p.excerpt LIKE %:keyword% " +
            "AND p.id NOT IN :excludeIds " +
            "ORDER BY p.createdAt DESC")
    List<Post> findByExcerptContainingAndPublishedExcluding(
            @Param("keyword") String keyword,
            @Param("excludeIds") Set<Long> excludeIds,
            Pageable pageable
    );
}