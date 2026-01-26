package com.blog.backend.feature.post.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PostStatus {

    PUBLISHED("published", "발행됨"),
    DELETED("deleted", "삭제됨");

    private final String key;
    private final String title;

    public static PostStatus fromKey(String key) {
        for (PostStatus status : values()) {
            if (status.key.equalsIgnoreCase(key)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown status: " + key);
    }
}