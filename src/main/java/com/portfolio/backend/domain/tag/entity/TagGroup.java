package com.portfolio.backend.domain.tag.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TagGroup {

    LANGUAGE("language", "언어"),
    FRAMEWORK("framework", "프레임워크"),
    AREA("area", "영역"),
    TOPIC("topic", "주제");

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