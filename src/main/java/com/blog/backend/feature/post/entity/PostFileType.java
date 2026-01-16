package com.blog.backend.feature.post.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 게시글 내 파일 역할 구분
 *
 * - THUMBNAIL: 게시글 대표 이미지 (게시글당 1개, Post.thumbnailUrl에 중복 저장)
 * - CONTENT: 본문 내 삽입된 파일 (여러 개 가능)
 */
@Getter
@RequiredArgsConstructor
public enum PostFileType {

    THUMBNAIL("thumbnail", "썸네일"),
    CONTENT("content", "본문 파일");

    private final String key;
    private final String title;
}