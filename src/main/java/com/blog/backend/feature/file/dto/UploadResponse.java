package com.blog.backend.feature.file.dto;

import com.blog.backend.feature.file.entity.FileMetadata;
import lombok.Builder;

@Builder
public record UploadResponse(
        Long id,
        String originalName,
        String url,
        String contentType,
        Long fileSize
) {
    public static UploadResponse from(FileMetadata fileMetadata) {
        return UploadResponse.builder()
                .id(fileMetadata.getId())
                .originalName(fileMetadata.getOriginalName())
                .url(fileMetadata.getUrl())
                .contentType(fileMetadata.getContentType())
                .fileSize(fileMetadata.getFileSize())
                .build();
    }
}