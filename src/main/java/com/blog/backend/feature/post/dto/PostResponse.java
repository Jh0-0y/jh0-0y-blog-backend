package com.blog.backend.feature.post.dto;

import com.blog.backend.feature.post.entity.Post;
import com.blog.backend.feature.post.entity.PostType;
import com.blog.backend.feature.post.entity.PostStatus;
import com.blog.backend.feature.stack.entity.Stack;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class PostResponse {

    /**
     * 게시글 목록 응답 (요약 정보)
     */
    @Getter
    @Builder
    public static class PostItems {

        private Long id;
        private String title;
        private String excerpt;
        private PostType postType;
        private PostStatus status;
        private String thumbnailUrl;  // 썸네일 URL 추가
        private List<String> tags;
        private List<String> stacks;
        private LocalDateTime createdAt;

        public static PostItems from(Post post) {
            return PostItems.builder()
                    .id(post.getId())
                    .title(post.getTitle())
                    .excerpt(post.getExcerpt())
                    .postType(post.getPostType())
                    .status(post.getStatus())
                    .thumbnailUrl(post.getThumbnailUrl())  // 썸네일 URL
                    .tags(post.getTags())
                    .stacks(post.getStacks().stream()
                            .map(Stack::getName)
                            .collect(Collectors.toList()))
                    .createdAt(post.getCreatedAt())
                    .build();
        }
    }

    /**
     * 게시글 상세 응답 (전체 정보)
     */
    @Getter
    @Builder
    public static class Detail {

        private Long id;
        private String title;
        private String excerpt;
        private PostType postType;
        private String content;
        private PostStatus status;
        private String thumbnailUrl;  // 썸네일 URL 추가
        private List<String> tags;
        private List<String> stacks;
        private Adjacent prev;
        private Adjacent next;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public static Detail from(Post post, Post prevPost, Post nextPost) {
            return Detail.builder()
                    .id(post.getId())
                    .title(post.getTitle())
                    .excerpt(post.getExcerpt())
                    .postType(post.getPostType())
                    .content(post.getContent())
                    .status(post.getStatus())
                    .thumbnailUrl(post.getThumbnailUrl())  // 썸네일 URL
                    .tags(post.getTags())
                    .stacks(post.getStacks().stream()
                            .map(Stack::getName)
                            .collect(Collectors.toList()))
                    .prev(prevPost != null ? Adjacent.from(prevPost) : null)
                    .next(nextPost != null ? Adjacent.from(nextPost) : null)
                    .createdAt(post.getCreatedAt())
                    .updatedAt(post.getUpdatedAt())
                    .build();
        }
    }

    /**
     * 인접 게시글 응답 (이전/다음)
     */
    @Getter
    @Builder
    public static class Adjacent {

        private Long id;
        private String title;
        private PostType postType;

        public static Adjacent from(Post post) {
            return Adjacent.builder()
                    .id(post.getId())
                    .title(post.getTitle())
                    .postType(post.getPostType())
                    .build();
        }
    }
}