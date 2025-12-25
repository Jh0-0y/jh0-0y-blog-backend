package com.portfolio.backend.domain.tag;

import com.portfolio.backend.domain.tag.dto.TagResponse;
import com.portfolio.backend.global.common.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {
    
    private final TagService tagService;
    
    // 전체 태그 조회
    @GetMapping
    public ResponseEntity<ApiResponse<List<TagResponse>>> getTags() {
        List<TagResponse> tags = tagService.getAllTags();
        return ResponseEntity.ok(ApiResponse.success(tags));
    }
}
