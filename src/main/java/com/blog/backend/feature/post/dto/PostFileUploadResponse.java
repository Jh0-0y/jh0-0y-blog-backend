package com.blog.backend.feature.post.dto;

import com.blog.backend.global.file.entity.FileMetadata;
import lombok.Builder;

@Builder
public record PostFileUploadResponse(
        Long id,
        String originalName,
        String url,
        Long size,
        String contentType
) {
    public static PostFileUploadResponse from(FileMetadata fileMetadata) {
        return PostFileUploadResponse.builder()
                .id(fileMetadata.getId())
                .originalName(fileMetadata.getOriginalName())
                .url(fileMetadata.getUrl())
                .size(fileMetadata.getSize())
                .contentType(fileMetadata.getContentType())
                .build();
    }
}