package com.blog.backend.global.file.dto;

import com.blog.backend.global.file.entity.FileMetadata;
import lombok.Builder;

@Builder
public record FileUploadResponse(
        Long id,
        String originalName,
        String url,
        Long size,
        String contentType
) {
    public static FileUploadResponse from(FileMetadata fileMetadata) {
        return FileUploadResponse.builder()
                .id(fileMetadata.getId())
                .originalName(fileMetadata.getOriginalName())
                .url(fileMetadata.getUrl())
                .size(fileMetadata.getSize())
                .contentType(fileMetadata.getContentType())
                .build();
    }
}