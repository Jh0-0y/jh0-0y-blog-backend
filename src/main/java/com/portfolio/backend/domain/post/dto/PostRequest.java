package com.portfolio.backend.domain.post.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Getter
@NoArgsConstructor
public class PostRequest {
    
    @NotBlank(message = "제목은 필수입니다")
    @Size(max = 255, message = "제목은 255자 이하여야 합니다")
    private String title;
    
    @NotBlank(message = "내용은 필수입니다")
    private String content;
    
    @Size(max = 500, message = "요약은 500자 이하여야 합니다")
    private String summary;
    
    private Long categoryId;
    
    private Set<String> tags = new HashSet<>();
    
    private boolean isPublished = false;
}
