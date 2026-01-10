package com.blog.backend.feature.stack.entity;

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
@Table(name = "stacks")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Stack extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StackGroup stackGroup;

    @ManyToMany(mappedBy = "stacks")
    private Set<Post> posts = new HashSet<>();

    @Builder
    public Stack(String name, StackGroup stackGroup) {
        this.name = name;
        this.stackGroup = stackGroup != null ? stackGroup : StackGroup.ETC;
    }

    /**
     * 스택에 연결된 공개 게시글 수
     */
    public long getPublicPostCount() {
        return posts.stream()
                .filter(Post::isPublic)
                .count();
    }

    /**
     * 스택명 변경
     */
    public void updateName(String name) {
        this.name = name;
    }

    /**
     * 스택 그룹 변경
     */
    public void updateStackGroup(StackGroup stackGroup) {
        this.stackGroup = stackGroup;
    }
}