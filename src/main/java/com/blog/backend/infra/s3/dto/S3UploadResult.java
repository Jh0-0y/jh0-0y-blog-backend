package com.blog.backend.infra.s3.dto;

import lombok.Builder;

@Builder
public record S3UploadResult(
        String originalName,
        String path,
        String contentType,
        long fileSize
) {
}