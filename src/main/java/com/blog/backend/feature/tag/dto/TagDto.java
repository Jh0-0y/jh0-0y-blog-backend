package com.blog.backend.feature.tag.dto;

import com.blog.backend.feature.tag.entity.Tag;
import com.blog.backend.feature.tag.entity.TagGroup;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

public class TagDto {

    // ========== Request ========== //

    /**
     * 태그 생성 요청
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {

        @NotBlank(message = "태그명을 입력해주세요")
        @Size(max = 50, message = "태그명은 50자 이내로 입력해주세요")
        private String name;

        private TagGroup tagGroup;
    }

    /**
     * 태그 수정 요청
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {

        @NotBlank(message = "태그명을 입력해주세요")
        @Size(max = 50, message = "태그명은 50자 이내로 입력해주세요")
        private String name;

        private TagGroup tagGroup;
    }

    // ========== Response ========== //

    /**
     * 태그 기본 응답
     */
    @Getter
    @Builder
    public static class Response {

        private Long id;
        private String name;
        private TagGroup tagGroup;

        public static Response from(Tag tag) {
            return Response.builder()
                    .id(tag.getId())
                    .name(tag.getName())
                    .tagGroup(tag.getTagGroup())
                    .build();
        }
    }

    /**
     * 태그 + 게시글 수 응답
     */
    @Getter
    @Builder
    public static class TagWithCountResponse {

        private Long id;
        private String name;
        private TagGroup tagGroup;
        private Long postCount;

        public static TagWithCountResponse of(Tag tag, Long postCount) {
            return TagWithCountResponse.builder()
                    .id(tag.getId())
                    .name(tag.getName())
                    .tagGroup(tag.getTagGroup())
                    .postCount(postCount)
                    .build();
        }
    }

    /**
     * 그룹별 태그 응답
     */
    @Getter
    @Builder
    public static class GroupedTagsResponse {

        private Map<TagGroup, List<TagWithCountResponse>> groupedTags;

        public static GroupedTagsResponse of(Map<TagGroup, List<TagWithCountResponse>> groupedTags) {
            return GroupedTagsResponse.builder()
                    .groupedTags(groupedTags)
                    .build();
        }
    }

    /**
     * 인기 태그 응답
     */
    @Getter
    @Builder
    public static class PopularTagResponse {

        private int rank;
        private Long id;
        private String name;
        private Long postCount;

        public static PopularTagResponse of(int rank, Tag tag, Long postCount) {
            return PopularTagResponse.builder()
                    .rank(rank)
                    .id(tag.getId())
                    .name(tag.getName())
                    .postCount(postCount)
                    .build();
        }
    }
}