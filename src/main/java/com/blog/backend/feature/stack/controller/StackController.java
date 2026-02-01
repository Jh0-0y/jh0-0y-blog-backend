package com.blog.backend.feature.stack.controller;

import com.blog.backend.feature.stack.dto.StackResponse;
import com.blog.backend.feature.stack.service.StackService;
import com.blog.backend.global.core.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 공개 스택 컨트롤러
 *
 * 스택 조회 API (인증 불필요)
 */
@RestController
@RequestMapping("/api/stacks")
@RequiredArgsConstructor
public class StackController {

    private final StackService stackService;

    /**
     * 전체 스택 목록 조회 (게시글 작성용)
     * GET /api/stacks
     *
     * - DB에 등록된 모든 스택 반환
     * - 그룹 정보 포함
     * - 게시글 수와 무관
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<StackResponse.StackItem>>> getAllStacks() {
        List<StackResponse.StackItem> stacks = stackService.getAllStacks();
        return ResponseEntity.ok(ApiResponse.success(stacks));
    }

    /**
     * 그룹별 스택 목록 조회 - 전체 (게시글 필터링용)
     * GET /api/stacks/grouped
     *
     * - 실제 사용 중인 스택만 반환
     * - 게시글 수 포함
     */
    @GetMapping("/grouped")
    public ResponseEntity<ApiResponse<StackResponse.GroupedStacks>> getGroupedStacks() {
        StackResponse.GroupedStacks groupedStacks = stackService.getGroupedStacksWithPostCount();
        return ResponseEntity.ok(ApiResponse.success(groupedStacks));
    }

    /**
     * 그룹별 스택 목록 조회 - 사용자별 (게시글 필터링용)
     * GET /api/stacks/grouped/user/{nickname}
     *
     * - 해당 사용자가 실제 사용 중인 스택만 반환
     * - 게시글 수 포함
     */
    @GetMapping("/grouped/user/{nickname}")
    public ResponseEntity<ApiResponse<StackResponse.GroupedStacks>> getGroupedStacksByUser(
            @PathVariable String nickname
    ) {
        StackResponse.GroupedStacks groupedStacks = stackService.getGroupedStacksWithPostCountByUser(nickname);
        return ResponseEntity.ok(ApiResponse.success(groupedStacks));
    }

    /**
     * 인기 스택 조회 (사이드바용)
     * GET /api/stacks/popular?limit=5
     */
    @GetMapping("/popular")
    public ResponseEntity<ApiResponse<List<StackResponse.PopularStack>>> getPopularStacks(
            @RequestParam(defaultValue = "5") int limit
    ) {
        List<StackResponse.PopularStack> popularStacks = stackService.getPopularStacks(limit);
        return ResponseEntity.ok(ApiResponse.success(popularStacks));
    }

    /**
     * 스택 자동완성 검색
     * GET /api/stacks/autocomplete?keyword=ja
     *
     * @param keyword 검색 키워드
     * @return 검색된 스택 목록 (최대 5개)
     */
    @GetMapping("/autocomplete")
    public ResponseEntity<ApiResponse<List<StackResponse.StackItem>>> autocomplete(
            @RequestParam(required = false, defaultValue = "") String keyword
    ) {
        List<StackResponse.StackItem> results = stackService.autocomplete(keyword);
        return ResponseEntity.ok(ApiResponse.success(results));
    }
}