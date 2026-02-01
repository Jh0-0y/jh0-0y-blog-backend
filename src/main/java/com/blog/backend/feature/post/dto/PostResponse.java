package com.blog.backend.feature.post.dto;

import com.blog.backend.feature.post.entity.PostType;
import com.blog.backend.feature.post.entity.PostStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

public class PostResponse {

    /**
     * 작성자 정보
     */
    @Getter
    @Builder
    public static class AuthorInfo {
        private String nickname;
        private String profileImagePath;

        public static AuthorInfo of(String nickname, String profileImageUrl) {
            return AuthorInfo.builder()
                    .nickname(nickname)
                    .profileImagePath(profileImageUrl)
                    .build();
        }
    }

    /**
     * 게시글 목록 응답 (요약 정보)
     */
    @Getter
    @Builder
    public static class PostItems {

        private Long id;
        private String slug;
        private String title;
        private String excerpt;
        private PostType postType;
        private PostStatus status;
        private String thumbnailPath;
        private List<String> tags;
        private List<String> stacks;
        private AuthorInfo author;
        private LocalDateTime createdAt;

        /**
         * 서비스 레이어에서 준비된 데이터로 DTO 생성
         * Lazy Loading 방지: 모든 데이터를 파라미터로 받음
         */
        public static PostItems of(
                Long id,
                String slug,
                String title,
                String excerpt,
                PostType postType,
                PostStatus status,
                String thumbnailPath,
                List<String> tags,
                List<String> stacks,
                AuthorInfo author,
                LocalDateTime createdAt
        ) {
            return PostItems.builder()
                    .id(id)
                    .slug(slug)
                    .title(title)
                    .excerpt(excerpt)
                    .postType(postType)
                    .status(status)
                    .thumbnailPath(thumbnailPath)
                    .tags(tags)
                    .stacks(stacks)
                    .author(author)
                    .createdAt(createdAt)
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
        private String slug;
        private String title;
        private String excerpt;
        private PostType postType;
        private String content;
        private PostStatus status;
        private String thumbnailPath;
        private List<String> tags;
        private List<String> stacks;
        private AuthorInfo author;
        private List<PostItems> relatedPosts;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        /**
         * 서비스 레이어에서 준비된 데이터로 DTO 생성
         * Lazy Loading 방지: 모든 데이터를 파라미터로 받음
         */
        public static Detail of(
                Long id,
                String slug,
                String title,
                String excerpt,
                PostType postType,
                String content,
                PostStatus status,
                String thumbnailPath,
                List<String> tags,
                List<String> stacks,
                AuthorInfo author,
                List<PostItems> relatedPosts,
                LocalDateTime createdAt,
                LocalDateTime updatedAt
        ) {
            return Detail.builder()
                    .id(id)
                    .slug(slug)
                    .title(title)
                    .excerpt(excerpt)
                    .postType(postType)
                    .content(content)
                    .status(status)
                    .thumbnailPath(thumbnailPath)
                    .tags(tags)
                    .stacks(stacks)
                    .author(author)
                    .relatedPosts(relatedPosts != null ? relatedPosts : List.of())
                    .createdAt(createdAt)
                    .updatedAt(updatedAt)
                    .build();
        }
    }
    /**
     * 게시글 수정용 응답 (현재 사용 중인 파일 ID 포함)
     * 프론트엔드에서 파일 관리를 위해:
     * - contentFileIds: 현재 사용 중인 본문 파일 ID 목록
     * - 프론트에서 이 정보를 기반으로 파일 삭제/추가 관리
     */
    @Getter
    @Builder
    public static class Edit {

        private Long id;
        private String slug;
        private String title;
        private String excerpt;
        private PostType postType;
        private String content;
        private PostStatus status;
        private String thumbnailPath;
        private List<String> tags;
        private List<String> stacks;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public static Edit of(
                Long id,
                String slug,
                String title,
                String excerpt,
                PostType postType,
                String content,
                PostStatus status,
                String thumbnailPath,
                List<String> tags,
                List<String> stacks,
                LocalDateTime createdAt,
                LocalDateTime updatedAt
        ) {
            return Edit.builder()
                    .id(id)
                    .slug(slug)
                    .title(title)
                    .excerpt(excerpt)
                    .postType(postType)
                    .content(content)
                    .status(status)
                    .thumbnailPath(thumbnailPath)
                    .tags(tags)
                    .stacks(stacks)
                    .createdAt(createdAt)
                    .updatedAt(updatedAt)
                    .build();
        }
    }
}