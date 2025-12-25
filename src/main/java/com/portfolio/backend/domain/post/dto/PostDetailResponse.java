package com.portfolio.backend.domain.post.dto;

import com.portfolio.backend.domain.category.dto.CategoryResponse;
import com.portfolio.backend.domain.post.Post;
import com.portfolio.backend.domain.tag.dto.TagResponse;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class PostDetailResponse {
    
    private Long id;
    private String title;
    private String content;
    private String summary;
    private CategoryResponse category;
    private List<TagResponse> tags;
    private boolean isPublished;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static PostDetailResponse from(Post post) {
        return PostDetailResponse.builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .summary(post.getSummary())
                .category(post.getCategory() != null 
                        ? CategoryResponse.from(post.getCategory()) 
                        : null)
                .tags(post.getTags().stream()
                        .map(TagResponse::from)
                        .collect(Collectors.toList()))
                .isPublished(post.isPublished())
                .createdAt(post.getCreatedAt())
                .updatedAt(post.getUpdatedAt())
                .build();
    }
}
