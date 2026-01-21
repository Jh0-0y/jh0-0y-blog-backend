package com.blog.backend.feature.stack.controller;

import com.blog.backend.feature.stack.dto.StackResponse;
import com.blog.backend.feature.stack.dto.StackRequest;
import com.blog.backend.feature.stack.entity.StackGroup;
import com.blog.backend.feature.stack.service.StackService;
import com.blog.backend.global.core.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stacks")
@RequiredArgsConstructor
public class StackController {

    private final StackService stackService;

    /**
     * 스택 생성
     * POST /api/stacks
     */
    @PostMapping
    public ResponseEntity<ApiResponse<StackResponse.Response>> createStack(
            @Valid @RequestBody StackRequest.Create request
    ) {
        StackResponse.Response response = stackService.createStack(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "스택이 생성되었습니다"));
    }

    /**
     * 스택 수정
     * PUT /api/stacks/{stackId}
     */
    @PutMapping("/{stackId}")
    public ResponseEntity<ApiResponse<StackResponse.Response>> updateStack(
            @PathVariable Long stackId,
            @Valid @RequestBody StackRequest.Update request
    ) {
        StackResponse.Response response = stackService.updateStack(stackId, request);
        return ResponseEntity.ok(ApiResponse.success(response, "스택이 수정되었습니다"));
    }

    /**
     * 스택 삭제
     * DELETE /api/stacks/{stackId}
     */
    @DeleteMapping("/{stackId}")
    public ResponseEntity<ApiResponse<Void>> deleteStack(
            @PathVariable Long stackId
    ) {
        stackService.deleteStack(stackId);
        return ResponseEntity.ok(ApiResponse.success(null, "스택이 삭제되었습니다"));
    }

    /**
     * 전체 스택 목록 조회
     * GET /api/stacks
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<StackResponse.Response>>> getAllStacks() {
        List<StackResponse.Response> stacks = stackService.getAllStacks();
        return ResponseEntity.ok(ApiResponse.success(stacks));
    }

    /**
     * 그룹별 스택 목록 조회
     * GET /api/stacks/group/{stackGroup}
     */
    @GetMapping("/group/{stackGroup}")
    public ResponseEntity<ApiResponse<List<StackResponse.Response>>> getStacksByGroup(
            @PathVariable StackGroup stackGroup
    ) {
        List<StackResponse.Response> stacks = stackService.getStacksByGroup(stackGroup);
        return ResponseEntity.ok(ApiResponse.success(stacks));
    }

    /**
     * 스택 + 게시글 수 목록 조회 (사이드바용)
     * GET /api/stacks/with-count
     */
    @GetMapping("/with-count")
    public ResponseEntity<ApiResponse<List<StackResponse.StackWithCount>>> getStacksWithPostCount() {
        List<StackResponse.StackWithCount> stacks = stackService.getStacksWithPostCount();
        return ResponseEntity.ok(ApiResponse.success(stacks));
    }

    /**
     * 그룹별 스택 + 게시글 수 목록 조회 (사이드바 All stacks용)
     * GET /api/stacks/grouped
     */
    @GetMapping("/grouped")
    public ResponseEntity<ApiResponse<StackResponse.GroupedStacks>> getGroupedStacks() {
        StackResponse.GroupedStacks groupedStacks = stackService.getGroupedStacksWithPostCount();
        return ResponseEntity.ok(ApiResponse.success(groupedStacks));
    }

    /**
     * 인기 스택 조회 (사이드바 Popular Stacks용)
     * GET /api/stacks/popular?limit=5
     */
    @GetMapping("/popular")
    public ResponseEntity<ApiResponse<List<StackResponse.PopularStack>>> getPopularStacks(
            @RequestParam(defaultValue = "5") int limit
    ) {
        List<StackResponse.PopularStack> popularStacks = stackService.getPopularStacks(limit);
        return ResponseEntity.ok(ApiResponse.success(popularStacks));
    }
}