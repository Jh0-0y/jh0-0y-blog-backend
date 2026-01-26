package com.blog.backend.feature.post.dto;

import com.blog.backend.feature.post.entity.PostStatus;
import com.blog.backend.feature.post.entity.PostType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

public class PostRequest {

    /**
     * 게시글 생성 요청
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Create {

        @NotBlank(message = "제목을 입력해주세요")
        @Size(max = 50, message = "제목은 50자 이내로 입력해주세요")
        private String title;

        @NotBlank(message = "요약을 입력해주세요")
        @Size(max = 200, message = "요약은 200자 이내로 입력해주세요")
        private String excerpt;

        @NotNull(message = "타입을 선택해주세요")
        private PostType postType;

        @NotBlank(message = "내용을 입력해주세요")
        @Size(max = 50000, message = "본문은 50000자 이내로 입력해주세요")
        private String content;

        private Long thumbnailFileId;

        private String thumbnailUrl;

        private List<String> tags;

        private Set<String> stacks;

    }

    /**
     * 게시글 수정 요청
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Update {

        @NotBlank(message = "제목을 입력해주세요")
        @Size(max = 50, message = "제목은 50자 이내로 입력해주세요")
        private String title;

        @NotBlank(message = "요약을 입력해주세요")
        @Size(max = 200, message = "요약은 200자 이내로 입력해주세요")
        private String excerpt;

        @NotNull(message = "타입을 선택해주세요")
        private PostType postType;

        @NotBlank(message = "내용을 입력해주세요")
        @Size(max = 50000, message = "본문은 50000자 이내로 입력해주세요")
        private String content;

        private Long thumbnailFileId;

        private String thumbnailUrl;

        private Boolean removeThumbnail;

        private List<String> tags;

        private Set<String> stacks;

    }
}