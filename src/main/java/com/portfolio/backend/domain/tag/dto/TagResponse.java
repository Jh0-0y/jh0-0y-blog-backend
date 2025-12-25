package com.portfolio.backend.domain.tag.dto;

import com.portfolio.backend.domain.tag.Tag;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TagResponse {
    
    private Long id;
    private String name;
    
    public static TagResponse from(Tag tag) {
        return TagResponse.builder()
                .id(tag.getId())
                .name(tag.getName())
                .build();
    }
}
