package com.blog.backend.feature.post.entity;

import com.blog.backend.global.error.CustomException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PostType {

    CORE("core", "중심"),
    ARCHITECTURE("architecture", "아키텍처"),
    TROUBLESHOOTING("troubleshooting", "트러블슈팅"),
    ESSAY("essay", "에세이");

    private final String key;
    private final String title;

    // 문자열로부터 enum 찾기
    public static PostType fromKey(String key) {
        for (PostType category : values()) {
            if (category.key.equalsIgnoreCase(key)) {
                return category;
            }
        }
        throw CustomException.badRequest("유효하지 않은 게시글 타입입니다 : " + key);
    }
}