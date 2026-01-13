package com.blog.backend.feature.post.entity;

import com.blog.backend.feature.stack.entity.Stack;
import com.blog.backend.feature.auth.entity.User;
import com.blog.backend.global.common.BaseTimeEntity;
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

    @Column(nullable = false, length = 500)
    private String excerpt;

    @Lob
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PostStatus status;

    @BatchSize(size = 100)
    @ElementCollection
    @CollectionTable(name = "post_tag", joinColumns = @JoinColumn(name = "post_id"))
    @Column(name = "tag")
    @OrderColumn(name = "order_idx")
    private List<String> tags = new ArrayList<>();

    @BatchSize(size = 100)
    @ManyToMany
    @JoinTable(
            name = "post_stack",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "stack_id")
    )
    @OrderBy("name ASC")
    private Set<Stack> stacks = new HashSet<>();

    // === 생성자 === //
    @Builder
    public Post(User user, PostType postType, String title, String excerpt, String content, PostStatus status) {
        this.user = user;
        this.postType = postType;
        this.title = title;
        this.excerpt = excerpt;
        this.content = content;
        this.status = status != null ? status : PostStatus.PRIVATE;
    }

    // === 비즈니스 로직 === //

    /**
     * 게시글 수정
     */
    public void update(PostType category, String title, String excerpt, String content, PostStatus status) {
        this.postType = category;
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
}