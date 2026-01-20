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

        private PostStatus status;

        private List<String> tags;

        private Set<String> stacks;

        /**
         * 본문 내 삽입된 파일 ID 목록
         * - 에디터에서 드래그 시 즉시 업로드된 파일들의 ID
         * - 게시글 저장 시점에 PostFile 매핑 생성 (fileType = CONTENT)
         * - null 또는 빈 리스트일 경우 본문 파일 연결 생략
         */
        private List<Long> contentsFileIds;

        // 썸네일은 Multipart로 별도 전송 (Controller에서 처리)
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

        private PostStatus status;

        private List<String> tags;

        private Set<String> stacks;

        /**
         * 새로 추가된 본문 파일 ID 목록
         * - 수정 시 새롭게 업로드된 파일들의 ID
         * - 기존 파일은 그대로 유지됨
         * - null 또는 빈 리스트일 경우 새 파일 연결 생략
         */
        private List<Long> contentsFileIds;

        /**
         * 삭제할 본문 파일 ID 목록
         * - 수정 시 제거하고 싶은 파일들의 ID
         * - PostFile 매핑만 삭제 (실제 파일은 스케줄러가 처리)
         * - null 또는 빈 리스트일 경우 파일 삭제 생략
         */
        private List<Long> deletedFileIds;

        /**
         * 썸네일 제거 플래그
         * - true: 기존 썸네일 삭제 (S3 + DB + PostFile 매핑)
         * - false/null: 썸네일 유지
         * - 새 썸네일 파일이 함께 전송되면 이 플래그는 무시됨 (교체 우선)
         */
        private Boolean removeThumbnail;

        // 썸네일 파일은 Multipart로 별도 전송 (Controller에서 처리)
    }
}