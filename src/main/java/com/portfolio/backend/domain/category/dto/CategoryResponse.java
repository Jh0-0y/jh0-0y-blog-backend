package com.portfolio.backend.domain.category.dto;

import com.portfolio.backend.domain.category.Category;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CategoryResponse {
    
    private Long id;
    private String name;
    private String slug;
    private Long postCount;
    
    public static CategoryResponse from(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .build();
    }
    
    public static CategoryResponse of(Category category, Long postCount) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .postCount(postCount)
                .build();
    }
}
