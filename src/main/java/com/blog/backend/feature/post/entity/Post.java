package com.blog.backend.feature.post.entity;

import com.blog.backend.feature.tag.entity.Tag;
import com.blog.backend.feature.auth.entity.User;
import com.blog.backend.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PostCategory category;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 500)
    private String excerpt;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PostStatus status;

    @ManyToMany
    @JoinTable(
            name = "post_tag",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @OrderBy("name ASC")
    private Set<Tag> tags = new HashSet<>();

    // === 생성자 === //
    @Builder
    public Post(User user, PostCategory category, String title, String excerpt, String content, PostStatus status) {
        this.user = user;
        this.category = category;
        this.title = title;
        this.excerpt = excerpt;
        this.content = content;
        this.status = status != null ? status : PostStatus.PRIVATE;
    }

    // === 비즈니스 로직 === //

    /**
     * 게시글 수정
     */
    public void update(PostCategory category, String title, String excerpt, String content, PostStatus status) {
        this.category = category;
        this.title = title;
        this.excerpt = excerpt;
        this.content = content;
        this.status = status;
    }

    /**
     * 공개 상태로 변경
     */
    public void publish() {
        this.status = PostStatus.PUBLIC;
    }

    /**
     * 비공개 상태로 변경
     */
    public void unpublish() {
        this.status = PostStatus.PRIVATE;
    }

    /**
     * 공개 여부 확인
     */
    public boolean isPublic() {
        return this.status == PostStatus.PUBLIC;
    }

    /**
     * 작성자 확인
     */
    public boolean isWrittenBy(Long userId) {
        return this.user.getId().equals(userId);
    }

    /**
     * 태그 추가
     */
    public void addTag(Tag tag) {
        this.tags.add(tag);
    }

    /**
     * 태그 제거
     */
    public void removeTag(Tag tag) {
        this.tags.remove(tag);
    }

    /**
     * 태그 전체 초기화
     */
    public void clearTags() {
        this.tags.clear();
    }

    /**
     * 태그 전체 교체
     */
    public void updateTags(Set<Tag> newTags) {
        this.tags.clear();
        this.tags.addAll(newTags);
    }
}