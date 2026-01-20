package com.blog.backend.feature.file.dto;

import com.blog.backend.feature.file.entity.FileMetadata;
import com.blog.backend.feature.file.entity.FileMetadataType;
import lombok.Builder;

@Builder
public record UploadResponse(
        Long id,
        String originalName,
        String url,
        Long fileSize,
        FileMetadataType fileMetadataType  // 추가: 프론트에서 파일 타입 확인용
) {
    public static UploadResponse from(FileMetadata fileMetadata) {
        return UploadResponse.builder()
                .id(fileMetadata.getId())
                .originalName(fileMetadata.getOriginalName())
                .url(fileMetadata.getUrl())
                .fileSize(fileMetadata.getFileSize())
                .fileMetadataType(fileMetadata.getFileMetadataType())  // 추가
                .build();
    }
}