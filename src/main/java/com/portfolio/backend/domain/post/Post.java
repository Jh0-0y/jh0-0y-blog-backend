package com.portfolio.backend.domain.post;

import com.portfolio.backend.domain.category.Category;
import com.portfolio.backend.domain.tag.Tag;
import com.portfolio.backend.domain.user.User;
import com.portfolio.backend.global.common.BaseEntity;
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String content;

    @Column(length = 500)
    private String summary;

    @Column(name = "is_published", nullable = false)
    private boolean isPublished = false;

    @ManyToMany
    @JoinTable(
            name = "post_tag",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();


    /**
     * 엔티티 비즈니스 로직
     */
    @Builder
    public Post(User user, Category category, String title, String content,
                String summary, boolean isPublished) {
        this.user = user;
        this.category = category;
        this.title = title;
        this.content = content;
        this.summary = summary;
        this.isPublished = isPublished;
    }

    // 글 수정
    public void update(String title, String content, String summary,
                       Category category, boolean isPublished) {
        this.title = title;
        this.content = content;
        this.summary = summary;
        this.category = category;
        this.isPublished = isPublished;
    }

    // 태그 관리
    public void addTag(Tag tag) {
        this.tags.add(tag);
    }

    public void removeTag(Tag tag) {
        this.tags.remove(tag);
    }

    public void clearTags() {
        this.tags.clear();
    }

    public void updateTags(Set<Tag> newTags) {
        this.tags.clear();
        this.tags.addAll(newTags);
    }

    // 공개 상태 변경
    public void publish() {
        this.isPublished = true;
    }

    public void unpublish() {
        this.isPublished = false;
    }
}
