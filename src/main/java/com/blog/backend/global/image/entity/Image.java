package com.blog.backend.global.image.entity;

import com.blog.backend.feature.post.entity.Post;
import com.blog.backend.global.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "images")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Image extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String originalName;  // 원본 파일명

    @Column(nullable = false)
    private String storedName;    // S3 저장 파일명

    @Column(nullable = false)
    private String url;           // S3 URL

    @Column(nullable = false)
    private String contentType;   // MIME 타입

    @Column(nullable = false)
    private Long fileSize;        // 파일 크기

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;            // 연결된 게시글 (null이면 임시 이미지)

    @Column(nullable = false)
    private boolean temporary;    // 임시 이미지 여부

    @Builder
    public Image(String originalName, String storedName, String url,
                 String contentType, Long fileSize) {
        this.originalName = originalName;
        this.storedName = storedName;
        this.url = url;
        this.contentType = contentType;
        this.fileSize = fileSize;
        this.temporary = true;  // 업로드 시 임시 상태
    }

    // 게시글에 연결
    public void attachToPost(Post post) {
        this.post = post;
        this.temporary = false;
    }

    // 게시글에서 분리 (고아 이미지로 전환)
    public void detachFromPost() {
        this.post = null;
        this.temporary = true;
    }
}