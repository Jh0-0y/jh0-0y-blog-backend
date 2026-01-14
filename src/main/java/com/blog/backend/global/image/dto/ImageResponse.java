package com.blog.backend.global.image.dto;

import com.blog.backend.global.image.entity.Image;
import lombok.Builder;

@Builder
public record ImageResponse(
        Long id,
        String originalName,
        String url,
        String contentType,
        Long fileSize
) {
    public static ImageResponse from(Image image) {
        return ImageResponse.builder()
                .id(image.getId())
                .originalName(image.getOriginalName())
                .url(image.getUrl())
                .contentType(image.getContentType())
                .fileSize(image.getFileSize())
                .build();
    }
}