package com.portfolio.backend.domain.category.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CategoryRequest {
    
    @NotBlank(message = "카테고리 이름은 필수입니다")
    @Size(max = 50, message = "카테고리 이름은 50자 이하여야 합니다")
    private String name;
    
    @NotBlank(message = "슬러그는 필수입니다")
    @Size(max = 50, message = "슬러그는 50자 이하여야 합니다")
    private String slug;
}
