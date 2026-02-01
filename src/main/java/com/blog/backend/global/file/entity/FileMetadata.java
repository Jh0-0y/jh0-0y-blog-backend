package com.blog.backend.global.file.entity;

import com.blog.backend.global.core.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 파일 메타데이터 엔티티
 *
 * 리팩터링 설계:
 * 1. postId 필드 제거 → 독립적인 파일 관리
 * 2. PostFile 중간 테이블을 통해 Post와 연결
 * 3. 고아 파일 판별: PostFile 테이블에 존재하지 않고 생성 후 24시간 경과한 파일
 * 4. 조회 최적화: Post.content에 URL 직접 포함 (JOIN 불필요)
 */
@Entity
@Table(name = "storage_file", indexes = {
        @Index(name = "idx_created_at", columnList = "created_at") // 고아 파일 정리용
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class FileMetadata extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 원본 파일명 (사용자가 업로드한 파일명)
     */
    @Column(nullable = false)
    private String originalName;

    /**
     * S3 전체 경로 (버킷 제외)
     */
    @Column(nullable = false, length = 500)
    private String path;

    /**
     * MIME 타입
     */
    @Column(nullable = false)
    private String contentType;

    /**
     * 파일 크기 (bytes)
     */
    @Column(nullable = false)
    private Long size;


    @Builder
    public FileMetadata(
            String originalName,
            String path,
            String contentType,
            Long size
    ) {
        this.originalName = originalName;
        this.path = path;
        this.contentType = contentType;
        this.size = size;
    }
}