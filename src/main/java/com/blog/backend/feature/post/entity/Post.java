package com.blog.backend.feature.post.entity;

import com.blog.backend.feature.stack.entity.Stack;
import com.blog.backend.feature.user.entity.User;
import com.blog.backend.global.core.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "post_type", nullable = false, length = 30)
    private PostType postType;

    @Column(nullable = false, unique = true, length = 100)
    private String title;

    /**
     * URL-safe한 게시글 식별자
     * - 제목 기반으로 자동 생성
     * - 한글 지원
     * - UNIQUE 제약조건
     * - 조회 시 ID 대신 사용
     */
    @Column(nullable = false, unique = true, length = 200)
    private String slug;

    @Column(nullable = false, length = 500)
    private String excerpt;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PostStatus status;

    /**
     * 썸네일 Path (Denormalization - 조회 성능 최적화)
     * - PostFile 테이블에도 매핑 정보 저장 (파일 추적용)
     * - Path는 빠른 조회를 위해 중복 저장
     * - null 허용 (썸네일 없는 게시글 가능)
     */
    @Column(name = "thumbnail_path", length = 1000)
    private String thumbnailPath;

    @BatchSize(size = 100)
    @ManyToMany
    @JoinTable(
            name = "post_stack",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "stack_id")
    )
    @OrderBy("name ASC")
    private Set<Stack> stacks = new HashSet<>();

    @BatchSize(size = 100)
    @ElementCollection
    @CollectionTable(name = "post_tag", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "tag")
    @OrderColumn(name = "order_idx")
    private List<String> tags = new ArrayList<>();

    // === 생성자 === //
    @Builder
    public Post(User user, PostType postType, String title, String slug, String excerpt,
                String content, PostStatus status, String thumbnailPath) {
        this.user = user;
        this.postType = postType;
        this.title = title;
        this.slug = slug;
        this.excerpt = excerpt;
        this.content = content;
        this.status = status != null ? status : PostStatus.DELETED;
        this.thumbnailPath = thumbnailPath;
    }

    // === 비즈니스 로직 === //

    /**
     * 게시글 수정
     */
    public void update(PostType category, String title, String slug, String excerpt, String content) {
        this.postType = category;
        this.title = title;
        this.slug = slug;
        this.excerpt = excerpt;
        this.content = content;
    }

    /**
     * slug 업데이트
     */
    public void updateSlug(String slug) {
        this.slug = slug;
    }

    /**
     * 발행됨 상태로 변경
     */
    public void publish() {
        this.status = PostStatus.PUBLISHED;
    }

    /**
     * 삭제됨 상태로 변경 (소프트 삭제)
     */
    public void unpublish() {
        this.status = PostStatus.DELETED;
    }

    /**
     * 소프트 삭제 처리
     * - status를 DELETED로 변경
     * - deletedAt 시간 기록 (BaseTimeEntity)
     */
    public void softDelete() {
        this.status = PostStatus.DELETED;
        this.markAsDeleted(); // BaseTimeEntity의 메서드 호출
    }

    /**
     * 삭제 복구
     * - status를 PUBLISHED로 변경
     * - deletedAt 초기화 (BaseTimeEntity)
     */
    public void restoreFromDelete() {
        this.status = PostStatus.PUBLISHED;
        this.restore(); // BaseTimeEntity의 메서드 호출
    }

    /**
     * 작성자 확인
     */
    public boolean isWrittenBy(Long userId) {
        return this.user.getId().equals(userId);
    }

    // === 자유 태그 / 스택 업데이트 ===
    public void updateTags(List<String> newTags) {
        this.tags.clear();
        this.tags.addAll(newTags);
    }

    /**
     * 스택 전체 초기화
     */
    public void clearStack() {
        this.stacks.clear();
    }

    /**
     * 스택 전체 교체
     */
    public void updateStacks(Set<Stack> newStacks) {
        this.stacks.clear();
        this.stacks.addAll(newStacks);
    }

    // === 썸네일 관리 === //

    /**
     * 썸네일 URL 업데이트
     */
    public void updateThumbnailUrl(String thumbnailUrl) {
        this.thumbnailPath = thumbnailUrl;
    }

    /**
     * 썸네일 제거
     */
    public void removeThumbnail() {
        this.thumbnailPath = null;
    }

    /**
     * 썸네일 존재 여부 확인
     */
    public boolean hasThumbnail() {
        return this.thumbnailPath != null && !this.thumbnailPath.isBlank();
    }
}