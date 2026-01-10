package com.blog.backend.feature.post.dto;

import com.blog.backend.feature.post.entity.PostType;
import com.blog.backend.feature.post.entity.PostStatus;
import lombok.Builder;
import lombok.Getter;

/**
 * 게시글 검색 조건
 * - 모든 조건은 optional (null이면 해당 조건 무시)
 */
@Getter
@Builder
public class PostSearchCondition {

    private PostType postType;
    private String stackName;
    private String keyword;
    private PostStatus status;

    /**
     * 공개 게시글 검색 조건 생성
     */
    public static PostSearchCondition ofPublic(PostType postType, String stackName, String keyword) {
        return PostSearchCondition.builder()
                .postType(postType)
                .stackName(stackName)
                .keyword(keyword)
                .status(PostStatus.PUBLIC)
                .build();
    }

    /**
     * 내 게시글 검색 조건 생성 (상태 무관)
     */
    public static PostSearchCondition ofMine(PostType postType, String stackName, String keyword) {
        return PostSearchCondition.builder()
                .postType(postType)
                .stackName(stackName)
                .keyword(keyword)
                .status(null)  // 상태 필터 없음
                .build();
    }
}