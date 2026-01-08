package com.blog.backend.feature.tag.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TagGroup {

    LANGUAGE("language", "언어"),
    FRAMEWORK("framework", "프레임워크"),
    LIBRARY("library","라이브러리"),
    DATABASE("database", "데이터베이스"),
    DEVOPS("devops", "데브옵스"),
    TOOL("tool", "툴"),
    ETC("etc", "기타");

    private final String key;
    private final String title;

    public static TagGroup fromKey(String key) {
        for (TagGroup group : values()) {
            if (group.key.equalsIgnoreCase(key)) {
                return group;
            }
        }
        throw new IllegalArgumentException("Unknown tag group: " + key);
    }
}