package com.portfolio.backend.domain.tag.entity;

import com.portfolio.backend.domain.post.entity.Post;
import com.portfolio.backend.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tags")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Tag extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TagGroup tagGroup;

    @ManyToMany(mappedBy = "tags")
    private Set<Post> posts = new HashSet<>();

    @Builder
    public Tag(String name, TagGroup tagGroup) {
        this.name = name;
        this.tagGroup = tagGroup != null ? tagGroup : TagGroup.TOPIC;
    }

    // 태그에 연결된 공개 게시글 수
    public long getPublicPostCount() {
        return posts.stream()
                .filter(Post::isPublic)
                .count();
    }
}