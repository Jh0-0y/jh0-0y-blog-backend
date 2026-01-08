package com.blog.backend.feature.post.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PostCategory {

    CORE("core", "중심"),
    ARCHITECTURE("architecture", "아키텍처"),
    TROUBLESHOOTING("troubleshooting", "트러블슈팅"),
    ESSAY("essay", "에세이");

    private final String key;
    private final String title;

    // 문자열로부터 enum 찾기
    public static PostCategory fromKey(String key) {
        for (PostCategory category : values()) {
            if (category.key.equalsIgnoreCase(key)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Unknown category: " + key);
    }
}