package com.portfolio.backend.domain.post.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PostStatus {

    PUBLIC("public", "공개"),
    PRIVATE("private", "비공개");

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