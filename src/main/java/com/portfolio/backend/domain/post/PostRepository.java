package com.portfolio.backend.domain.post;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    
    // 공개된 글 조회 (페이징)
    Page<Post> findByIsPublishedTrue(Pageable pageable);
    
    // 카테고리별 공개 글 조회
    Page<Post> findByCategoryIdAndIsPublishedTrue(Long categoryId, Pageable pageable);
    
    // 공개된 글 상세 조회 (fetch join)
    @Query("SELECT p FROM Post p " +
           "LEFT JOIN FETCH p.category " +
           "LEFT JOIN FETCH p.tags " +
           "WHERE p.id = :id AND p.isPublished = true")
    Optional<Post> findPublishedByIdWithDetails(@Param("id") Long id);
    
    // 관리자용 글 상세 조회 (공개 여부 무관)
    @Query("SELECT p FROM Post p " +
           "LEFT JOIN FETCH p.category " +
           "LEFT JOIN FETCH p.tags " +
           "WHERE p.id = :id")
    Optional<Post> findByIdWithDetails(@Param("id") Long id);
    
    // 최근 글 조회
    @Query("SELECT p FROM Post p " +
           "LEFT JOIN FETCH p.category " +
           "WHERE p.isPublished = true " +
           "ORDER BY p.createdAt DESC")
    List<Post> findRecentPosts(Pageable pageable);
    
    // 태그로 글 검색
    @Query("SELECT DISTINCT p FROM Post p " +
           "JOIN p.tags t " +
           "WHERE t.name = :tagName AND p.isPublished = true")
    Page<Post> findByTagName(@Param("tagName") String tagName, Pageable pageable);
}
