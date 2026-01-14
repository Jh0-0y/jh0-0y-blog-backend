package com.blog.backend.global.image.repository;


import com.blog.backend.feature.post.entity.Post;
import com.blog.backend.global.image.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ImageRepository extends JpaRepository<Image, Long> {

    // URL로 이미지 조회
    Optional<Image> findByUrl(String url);

    // 여러 URL로 이미지 조회
    List<Image> findByUrlIn(List<String> urls);

    // 게시글에 연결된 이미지 조회
    List<Image> findByPost(Post post);

    // 게시글 ID로 이미지 조회
    List<Image> findByPostId(Long postId);

    // 임시 이미지 중 일정 시간 지난 것 조회 (고아 파일 정리용)
    @Query("SELECT i FROM Image i WHERE i.temporary = true AND i.createdAt < :threshold")
    List<Image> findOrphanImages(@Param("threshold") LocalDateTime threshold);

    // 게시글의 이미지 연결 해제
    @Modifying
    @Query("UPDATE Image i SET i.post = null, i.temporary = true WHERE i.post.id = :postId")
    void detachAllFromPost(@Param("postId") Long postId);

    // 특정 URL 제외하고 게시글 이미지 연결 해제
    @Modifying
    @Query("UPDATE Image i SET i.post = null, i.temporary = true WHERE i.post.id = :postId AND i.url NOT IN :urls")
    void detachUnusedFromPost(@Param("postId") Long postId, @Param("urls") List<String> urls);
}