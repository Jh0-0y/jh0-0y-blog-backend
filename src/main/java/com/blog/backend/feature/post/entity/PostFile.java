package com.blog.backend.feature.post.entity;

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
 * - fileType으로 썸네일과 본문 파일 구분
 * - 썸네일: 게시글당 1개, 교체 시 기존 파일 즉시 삭제
 * - 본문 파일: 여러 개, 고아 파일은 스케줄러가 24시간 후 삭제
 */
@Entity
@Table(name = "post_file", indexes = {
        @Index(name = "idx_post_id", columnList = "post_id"),
        @Index(name = "idx_file_id", columnList = "file_id"),
        @Index(name = "idx_post_file_type", columnList = "post_id, file_type")
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
     * 파일 역할 구분
     * - THUMBNAIL: 썸네일 (게시글당 1개)
     * - CONTENT: 본문 내 파일 (여러 개)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "file_type", nullable = false, length = 20)
    private PostFileType fileType;

    /**
     * 파일 순서 (선택사항, 추후 확장용)
     * 본문 파일의 표시 순서를 지정할 때 사용
     */
    @Column(name = "display_order")
    private Integer displayOrder;

    @Builder
    public PostFile(Long postId, Long fileId, PostFileType fileType, Integer displayOrder) {
        this.postId = postId;
        this.fileId = fileId;
        this.fileType = fileType;
        this.displayOrder = displayOrder;
    }

    /**
     * 정적 팩토리 메서드 - 썸네일 매핑 생성
     */
    public static PostFile ofThumbnail(Long postId, Long fileId) {
        return PostFile.builder()
                .postId(postId)
                .fileId(fileId)
                .fileType(PostFileType.THUMBNAIL)
                .build();
    }

    /**
     * 정적 팩토리 메서드 - 본문 파일 매핑 생성
     */
    public static PostFile ofContent(Long postId, Long fileId) {
        return PostFile.builder()
                .postId(postId)
                .fileId(fileId)
                .fileType(PostFileType.CONTENT)
                .build();
    }

    /**
     * 정적 팩토리 메서드 - 순서 지정 본문 파일 매핑 생성
     */
    public static PostFile ofContent(Long postId, Long fileId, Integer displayOrder) {
        return PostFile.builder()
                .postId(postId)
                .fileId(fileId)
                .fileType(PostFileType.CONTENT)
                .displayOrder(displayOrder)
                .build();
    }
}