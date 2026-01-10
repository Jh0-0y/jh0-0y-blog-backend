package com.blog.backend.feature.stack.entity;

import com.blog.backend.global.error.CustomException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StackGroup {

    LANGUAGE("language", "언어"),
    FRAMEWORK("framework", "프레임워크"),
    LIBRARY("library","라이브러리"),
    DATABASE("database", "데이터베이스"),
    DEVOPS("devops", "데브옵스"),
    TOOL("tool", "툴"),
    ETC("etc", "기타");

    private final String key;
    private final String title;

    public static StackGroup fromKey(String key) {
        for (StackGroup group : values()) {
            if (group.key.equalsIgnoreCase(key)) {
                System.out.println("나여");
                return group;
            }
        }
        throw CustomException.badRequest("유효하지 않은 스택 그룹입니다 : " + key);
    }
}