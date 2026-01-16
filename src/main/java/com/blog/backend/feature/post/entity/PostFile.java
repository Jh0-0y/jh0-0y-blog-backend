package com.blog.backend.feature.post.entity;

import com.blog.backend.feature.file.entity.FileMetadata;
import com.blog.backend.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Post와 FileMetadata 간의 매핑 테이블
 *
 * 설계 의도:
 * - Post와 File은 독립적으로 관리되며, 이 테이블을 통해서만 연결됨
 * - 추후 파일 재사용, 다중 참조 등 확장 가능
 * - 고아 파일 판별: 이 테이블에 존재하지 않는 FileMetadata는 삭제 대상
 */
@Entity
@Table(name = "post_file", indexes = {
        @Index(name = "idx_post_id", columnList = "post_id"),
        @Index(name = "idx_file_id", columnList = "file_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PostFile extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 게시글 ID (외래키)
     */
    @Column(name = "post_id", nullable = false)
    private Long postId;

    /**
     * 파일 메타데이터 ID (외래키)
     */
    @Column(name = "file_id", nullable = false)
    private Long fileId;

    /**
     * 파일 순서 (선택사항, 추후 확장용)
     * 게시글 내 파일 표시 순서를 지정할 때 사용
     */
    @Column(name = "display_order")
    private Integer displayOrder;

    @Builder
    public PostFile(Long postId, Long fileId, Integer displayOrder) {
        this.postId = postId;
        this.fileId = fileId;
        this.displayOrder = displayOrder;
    }

    /**
     * 정적 팩토리 메서드 - 기본 매핑 생성
     */
    public static PostFile of(Long postId, Long fileId) {
        return PostFile.builder()
                .postId(postId)
                .fileId(fileId)
                .build();
    }

    /**
     * 정적 팩토리 메서드 - 순서 지정 매핑 생성
     */
    public static PostFile of(Long postId, Long fileId, Integer displayOrder) {
        return PostFile.builder()
                .postId(postId)
                .fileId(fileId)
                .displayOrder(displayOrder)
                .build();
    }
}