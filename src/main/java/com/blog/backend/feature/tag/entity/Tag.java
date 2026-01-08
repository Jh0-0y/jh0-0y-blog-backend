package com.blog.backend.feature.tag.entity;

import com.blog.backend.feature.post.entity.Post;
import com.blog.backend.global.common.BaseEntity;
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
        this.tagGroup = tagGroup != null ? tagGroup : TagGroup.ETC;
    }

    /**
     * 태그에 연결된 공개 게시글 수
     */
    public long getPublicPostCount() {
        return posts.stream()
                .filter(Post::isPublic)
                .count();
    }

    /**
     * 태그명 변경
     */
    public void updateName(String name) {
        this.name = name;
    }

    /**
     * 태그 그룹 변경
     */
    public void updateTagGroup(TagGroup tagGroup) {
        this.tagGroup = tagGroup;
    }
}