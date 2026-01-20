package com.blog.backend.feature.post.repository;

import com.blog.backend.feature.post.entity.Post;
import com.blog.backend.feature.post.entity.PostType;
import com.blog.backend.feature.post.entity.PostStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long>, JpaSpecificationExecutor<Post> {

    // ========== 존재 여부 확인 ========== //

    /**
     * 특정 제목을 가진 게시글 존재 여부 확인
     *
     * @param title 게시글 제목
     * @return 존재하면 true, 없으면 false
     */
    boolean existsByTitle(String title);

    /**
     * 특정 slug를 가진 게시글 존재 여부 확인
     *
     * @param slug 게시글 slug
     * @return 존재하면 true, 없으면 false
     */
    boolean existsBySlug(String slug);

    // ========== Slug 기반 조회 ========== //

    /**
     * slug로 게시글 조회
     *
     * @param slug 게시글 slug
     * @return 게시글 (Optional)
     */
    Optional<Post> findBySlug(String slug);

    /**
     * slug로 게시글 상세 조회 (스택, 태그 정보 함께 로딩)
     * - N+1 문제 방지를 위한 fetch join
     *
     * @param slug 게시글 slug
     * @return 스택과 태그가 포함된 게시글
     */
    @Query("SELECT p FROM Post p " +
            "LEFT JOIN FETCH p.stacks " +
            "LEFT JOIN FETCH p.tags " +
            "WHERE p.slug = :slug")
    Optional<Post> findBySlugWithStacks(@Param("slug") String slug);

    /**
     * slug 패턴으로 시작하는 게시글 개수 조회 (중복 slug 처리용)
     *
     * 예시:
     * - "react-입문-가이드" 로 시작하는 slug 개수
     * - "react-입문-가이드", "react-입문-가이드-2", "react-입문-가이드-3" 등
     *
     * @param slugPattern slug 패턴 (예: "react-입문-가이드%")
     * @return 해당 패턴으로 시작하는 게시글 개수
     */
    @Query("SELECT COUNT(p) FROM Post p WHERE p.slug LIKE :slugPattern")
    long countBySlugStartingWith(@Param("slugPattern") String slugPattern);

    // ========== ID 기반 조회 (Deprecated) ========== //

    /**
     * 게시글 상세 조회 (스택 정보 함께 로딩)
     * - N+1 문제 방지를 위한 fetch join
     *
     * @param id 게시글 ID
     * @return 스택이 포함된 게시글
     * @deprecated Use {@link #findBySlugWithStacks(String)} instead
     */
    @Deprecated
    @Query("SELECT p FROM Post p " +
            "LEFT JOIN FETCH p.stacks " +
            "LEFT JOIN FETCH p.tags " +
            "WHERE p.id = :id")
    Optional<Post> findByIdWithStacks(@Param("id") Long id);

    // ========== 목록 조회 ========== //

    @EntityGraph(attributePaths = {"stacks", "tags"})
    @Override
    Page<Post> findAll(Specification<Post> spec, Pageable pageable);

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
     * 게시글 타입 별 공개 게시글 목록 조회
     *
     * @param postType 게시글 타입
     * @param status 게시글 상태
     * @param pageable 페이징 정보
     * @return 해당 게시글 타입의 공개 게시글 페이지
     */
    Page<Post> findByPostTypeAndStatus(PostType postType, PostStatus status, Pageable pageable);

    /**
     * 스택명으로 공개 게시글 목록 조회
     * - Post와 Stack의 다대다 관계를 통해 조회
     *
     * @param stackName 스택명
     * @param status 게시글 상태
     * @param pageable 페이징 정보
     * @return 해당 스택이 포함된 공개 게시글 페이지
     */
    @Query("SELECT p FROM Post p JOIN p.stacks s WHERE s.name = :stackName AND p.status = :status")
    Page<Post> findByStackNameAndStatus(@Param("stackName") String stackName,
                                        @Param("status") PostStatus status,
                                        Pageable pageable);

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

    /**
     * 1순위 관련 게시글 조회
     * - Stack 교집합 많음 + PostType 일치 + 최신순
     * - 현재 게시글 제외
     * - 공개 게시글만
     * - **tags와 stacks를 함께 fetch join으로 로딩**
     */
    @Query("SELECT DISTINCT p FROM Post p " +
            "LEFT JOIN FETCH p.stacks " +
            "LEFT JOIN FETCH p.tags " +
            "JOIN p.stacks s " +
            "WHERE p.id != :currentPostId " +
            "AND p.status = :status " +
            "AND p.postType = :postType " +
            "AND s.name IN :stackNames " +
            "GROUP BY p.id " +
            "ORDER BY COUNT(s.id) DESC, p.createdAt DESC")
    List<Post> findRelatedPostsByStackAndType(
            @Param("currentPostId") Long currentPostId,
            @Param("stackNames") List<String> stackNames,
            @Param("postType") PostType postType,
            @Param("status") PostStatus status,
            Pageable pageable
    );

    /**
     * 2순위 관련 게시글 조회
     * - Stack 교집합 많음 + PostType 다름 + 최신순
     * - 현재 게시글 제외
     * - 공개 게시글만
     * - **tags와 stacks를 함께 fetch join으로 로딩**
     */
    @Query("SELECT DISTINCT p FROM Post p " +
            "LEFT JOIN FETCH p.stacks " +
            "LEFT JOIN FETCH p.tags " +
            "JOIN p.stacks s " +
            "WHERE p.id != :currentPostId " +
            "AND p.status = :status " +
            "AND p.postType != :postType " +
            "AND s.name IN :stackNames " +
            "GROUP BY p.id " +
            "ORDER BY COUNT(s.id) DESC, p.createdAt DESC")
    List<Post> findRelatedPostsByStackOnly(
            @Param("currentPostId") Long currentPostId,
            @Param("stackNames") List<String> stackNames,
            @Param("postType") PostType postType,
            @Param("status") PostStatus status,
            Pageable pageable
    );

    /**
     * 3순위 관련 게시글 조회 (보조)
     * - Stack이 없거나 관련 게시글이 부족할 때 사용
     * - 최신 공개 게시글
     * - 현재 게시글 제외
     * - **tags와 stacks를 함께 fetch join으로 로딩**
     */
    @Query("SELECT p FROM Post p " +
            "LEFT JOIN FETCH p.stacks " +
            "LEFT JOIN FETCH p.tags " +
            "WHERE p.id != :currentPostId " +
            "AND p.status = :status " +
            "ORDER BY p.createdAt DESC")
    List<Post> findLatestPublicPosts(
            @Param("currentPostId") Long currentPostId,
            @Param("status") PostStatus status,
            Pageable pageable
    );
}