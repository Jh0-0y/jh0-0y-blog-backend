package com.blog.backend.feature.tag.controller;

import com.blog.backend.feature.tag.dto.TagDto;
import com.blog.backend.feature.tag.entity.TagGroup;
import com.blog.backend.feature.tag.service.TagService;
import com.blog.backend.global.common.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    /**
     * 태그 생성
     * POST /api/tags
     */
    @PostMapping
    public ResponseEntity<ApiResponse<TagDto.Response>> createTag(
            @Valid @RequestBody TagDto.CreateRequest request
    ) {
        TagDto.Response response = tagService.createTag(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "태그가 생성되었습니다"));
    }

    /**
     * 태그 수정
     * PUT /api/tags/{tagId}
     */
    @PutMapping("/{tagId}")
    public ResponseEntity<ApiResponse<TagDto.Response>> updateTag(
            @PathVariable Long tagId,
            @Valid @RequestBody TagDto.UpdateRequest request
    ) {
        TagDto.Response response = tagService.updateTag(tagId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "태그가 수정되었습니다"));
    }

    /**
     * 태그 삭제
     * DELETE /api/tags/{tagId}
     */
    @DeleteMapping("/{tagId}")
    public ResponseEntity<ApiResponse<Void>> deleteTag(
            @PathVariable Long tagId
    ) {
        tagService.deleteTag(tagId);
        return ResponseEntity.ok(ApiResponse.success(null, "태그가 삭제되었습니다"));
    }

    /**
     * 전체 태그 목록 조회
     * GET /api/tags
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<TagDto.Response>>> getAllTags() {
        List<TagDto.Response> tags = tagService.getAllTags();
        return ResponseEntity.ok(ApiResponse.success(tags));
    }

    /**
     * 그룹별 태그 목록 조회
     * GET /api/tags/group/{tagGroup}
     */
    @GetMapping("/group/{tagGroup}")
    public ResponseEntity<ApiResponse<List<TagDto.Response>>> getTagsByGroup(
            @PathVariable TagGroup tagGroup
    ) {
        List<TagDto.Response> tags = tagService.getTagsByGroup(tagGroup);
        return ResponseEntity.ok(ApiResponse.success(tags));
    }

    /**
     * 태그 + 게시글 수 목록 조회 (사이드바용)
     * GET /api/tags/with-count
     */
    @GetMapping("/with-count")
    public ResponseEntity<ApiResponse<List<TagDto.TagWithCountResponse>>> getTagsWithPostCount() {
        List<TagDto.TagWithCountResponse> tags = tagService.getTagsWithPostCount();
        return ResponseEntity.ok(ApiResponse.success(tags));
    }

    /**
     * 그룹별 태그 + 게시글 수 목록 조회 (사이드바 All Tags용)
     * GET /api/tags/grouped
     */
    @GetMapping("/grouped")
    public ResponseEntity<ApiResponse<TagDto.GroupedTagsResponse>> getGroupedTags() {
        TagDto.GroupedTagsResponse groupedTags = tagService.getGroupedTagsWithPostCount();
        return ResponseEntity.ok(ApiResponse.success(groupedTags));
    }

    /**
     * 인기 태그 조회 (사이드바 Popular Tags용)
     * GET /api/tags/popular?limit=5
     */
    @GetMapping("/popular")
    public ResponseEntity<ApiResponse<List<TagDto.PopularTagResponse>>> getPopularTags(
            @RequestParam(defaultValue = "5") int limit
    ) {
        List<TagDto.PopularTagResponse> popularTags = tagService.getPopularTags(limit);
        return ResponseEntity.ok(ApiResponse.success(popularTags));
    }
}