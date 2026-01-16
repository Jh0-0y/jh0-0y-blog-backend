package com.blog.backend.feature.file.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 파일 타입별 저장 경로 전략을 정의하는 Enum
 */
@Getter
@RequiredArgsConstructor
public enum FileMetadataType {

    IMAGE("public/images", true, true),
    THUMBNAIL("public/images", true, false),
    VIDEO("public/videos", true, true),
    DOCUMENT("public/documents", true, true),
    PROFILE_IMAGE("public/users/profile", false, false);

    private final String basePathTemplate;
    private final boolean useDatePath;
    private final boolean useUuidFileName;

    public String getFullPath() {
        if (useDatePath) {
            LocalDateTime now = LocalDateTime.now();
            String datePath = now.format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
            return basePathTemplate + "/" + datePath;
        }
        return basePathTemplate;
    }

    public String generateFileName(Long identifier, String extension) {
        if (useUuidFileName) {
            return java.util.UUID.randomUUID().toString() + extension;
        }

        return switch (this) {
            case THUMBNAIL -> "thumbnail_" + identifier + extension;  // thumbnail_{postId}.jpg
            case PROFILE_IMAGE -> "profile_" + identifier + extension;
            default -> java.util.UUID.randomUUID().toString() + extension;
        };
    }
}