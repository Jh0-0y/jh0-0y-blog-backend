package com.blog.backend.feature.stack.dto;

import com.blog.backend.feature.stack.entity.Stack;
import com.blog.backend.feature.stack.entity.StackGroup;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

public class StackResponse {

    /**
     * 태그 기본 응답
     */
    @Getter
    @Builder
    public static class Response {

        private Long id;
        private String name;
        private StackGroup stackGroup;

        public static Response from(Stack stack) {
            return Response.builder()
                    .id(stack.getId())
                    .name(stack.getName())
                    .stackGroup(stack.getStackGroup())
                    .build();
        }
    }

    /**
     * 태그 + 게시글 수 응답
     */
    @Getter
    @Builder
    public static class StackWithCount {

        private Long id;
        private String name;
        private StackGroup stackGroup;
        private Long postCount;

        public static StackWithCount of(Stack stack, Long postCount) {
            return StackWithCount.builder()
                    .id(stack.getId())
                    .name(stack.getName())
                    .stackGroup(stack.getStackGroup())
                    .postCount(postCount)
                    .build();
        }
    }

    /**
     * 그룹별 태그 응답
     */
    @Getter
    @Builder
    public static class GroupedStacks {

        private Map<StackGroup, List<StackWithCount>> groupedTags;

        public static GroupedStacks of(Map<StackGroup, List<StackWithCount>> groupedTags) {
            return GroupedStacks.builder()
                    .groupedTags(groupedTags)
                    .build();
        }
    }

    /**
     * 인기 태그 응답
     */
    @Getter
    @Builder
    public static class PopularStack {

        private int rank;
        private Long id;
        private String name;
        private Long postCount;

        public static PopularStack of(int rank, Stack stack, Long postCount) {
            return PopularStack.builder()
                    .rank(rank)
                    .id(stack.getId())
                    .name(stack.getName())
                    .postCount(postCount)
                    .build();
        }
    }
}