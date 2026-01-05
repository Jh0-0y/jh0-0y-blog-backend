package com.portfolio.backend.domain.post.dto;

import com.portfolio.backend.domain.post.entity.Post;
import com.portfolio.backend.domain.post.entity.PostCategory;
import com.portfolio.backend.domain.post.entity.PostStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PostDto {

    // ========== Request ========== //

    /**
     * 게시글 생성 요청
     */
    @Getter
    @Builder
    public static class CreateRequest {

        @NotBlank(message = "제목을 입력해주세요")
        @Size(max = 100, message = "제목은 100자 이내로 입력해주세요")
        private String title;

        @NotBlank(message = "요약을 입력해주세요")
        @Size(max = 500, message = "요약은 500자 이내로 입력해주세요")
        private String excerpt;

        @NotNull(message = "카테고리를 선택해주세요")
        private PostCategory category;

        @NotBlank(message = "내용을 입력해주세요")
        private String content;

        private PostStatus status;

        private Set<String> tags;
    }

    /**
     * 게시글 수정 요청
     */
    @Getter
    @Builder
    public static class UpdateRequest {

        @NotBlank(message = "제목을 입력해주세요")
        @Size(max = 100, message = "제목은 100자 이내로 입력해주세요")
        private String title;

        @NotBlank(message = "요약을 입력해주세요")
        @Size(max = 500, message = "요약은 500자 이내로 입력해주세요")
        private String excerpt;

        @NotNull(message = "카테고리를 선택해주세요")
        private PostCategory category;

        @NotBlank(message = "내용을 입력해주세요")
        private String content;

        private PostStatus status;

        private Set<String> tags;
    }

    // ========== Response ========== //

    /**
     * 게시글 목록 응답 (요약 정보)
     */
    @Getter
    @Builder
    public static class ListResponse {

        private Long id;
        private String title;
        private String excerpt;
        private PostCategory category;
        private PostStatus status;
        private List<String> tags;
        private LocalDateTime createdAt;

        public static ListResponse from(Post post) {
            return ListResponse.builder()
                    .id(post.getId())
                    .title(post.getTitle())
                    .excerpt(post.getExcerpt())
                    .category(post.getCategory())
                    .status(post.getStatus())
                    .tags(post.getTags().stream()
                            .map(tag -> tag.getName())
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
    public static class DetailResponse {

        private Long id;
        private String title;
        private String excerpt;
        private PostCategory category;
        private String content;
        private PostStatus status;
        private List<String> tags;
        private AdjacentPostResponse prev;
        private AdjacentPostResponse next;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public static DetailResponse from(Post post, Post prevPost, Post nextPost) {
            return DetailResponse.builder()
                    .id(post.getId())
                    .title(post.getTitle())
                    .excerpt(post.getExcerpt())
                    .category(post.getCategory())
                    .content(post.getContent())
                    .status(post.getStatus())
                    .tags(post.getTags().stream()
                            .map(tag -> tag.getName())
                            .collect(Collectors.toList()))
                    .prev(prevPost != null ? AdjacentPostResponse.from(prevPost) : null)
                    .next(nextPost != null ? AdjacentPostResponse.from(nextPost) : null)
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
    public static class AdjacentPostResponse {

        private Long id;
        private String title;
        private PostCategory category;

        public static AdjacentPostResponse from(Post post) {
            return AdjacentPostResponse.builder()
                    .id(post.getId())
                    .title(post.getTitle())
                    .category(post.getCategory())
                    .build();
        }
    }
}